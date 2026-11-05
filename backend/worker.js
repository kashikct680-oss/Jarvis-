// JARVIS Gemini API proxy for Cloudflare Workers.
// Store GEMINI_API_KEY as a Cloudflare Worker secret.
// Never put your Gemini API key in this file.

export default {
  async fetch(request, env) {
    const cors = {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type, Authorization"
    };

    if (request.method === "OPTIONS") {
      return new Response(null, { headers: cors });
    }

    if (request.method !== "POST") {
      return json({ error: "Use POST" }, 405, cors);
    }

    if (!env.GEMINI_API_KEY) {
      return json(
        { error: "Server is missing GEMINI_API_KEY" },
        500,
        cors
      );
    }

    try {
      const body = await request.json();
      const messages = Array.isArray(body.messages)
        ? body.messages.slice(-16)
        : [];

      const contents = messages.map(m => ({
        role:
          m.role === "assistant" || m.role === "model"
            ? "model"
            : "user",
        parts: [{
          text: String(m.content || "").slice(0, 12000)
        }]
      })).filter(m => m.parts[0].text.trim());

      if (!contents.length) {
        return json({ error: "No message supplied" }, 400, cors);
      }

      const endpoint =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" +
        encodeURIComponent(env.GEMINI_API_KEY);

      const upstream = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          systemInstruction: {
            parts: [{
              text: "You are JARVIS, a helpful Android assistant. Be concise, clear, and safe. Never claim to have performed a phone action unless the Android app confirms it. Ask before sensitive actions."
            }]
          },
          contents,
          tools: [{ google_search: {} }],
          generationConfig: {
            temperature: 0.6,
            maxOutputTokens: 1200
          }
        })
      });

      const data = await upstream.json();

      if (!upstream.ok) {
        return json(
          { error: data.error?.message || "Gemini request failed" },
          upstream.status,
          cors
        );
      }

      const text = (
        data.candidates?.[0]?.content?.parts || []
      ).map(p => p.text || "").join("\n").trim();

      return json(
        { text: text || "I could not generate a response." },
        200,
        cors
      );

    } catch (e) {
      return json(
        { error: "Request failed: " + String(e) },
        500,
        cors
      );
    }
  }
};

function json(value, status, headers) {
  return new Response(JSON.stringify(value), {
    status,
    headers: {
      "Content-Type": "application/json",
      ...headers
    }
  });
}
