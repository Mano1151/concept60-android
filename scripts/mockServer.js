const http = require('http');

const PORT = process.env.MOCK_PORT || 5000;

function parseJSON(req) {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', (chunk) => (body += chunk));
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch (e) {
        resolve(null);
      }
    });
  });
}

const server = http.createServer(async (req, res) => {
  const { method, url, headers } = req;

  if (url === '/api/history' && method === 'GET') {
    const auth = headers.authorization;
    if (!auth) {
      res.writeHead(401, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ message: 'Authentication required.' }));
      return;
    }
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify([{ id: '1', query: 'Example' }]));
    return;
  }

  if (url === '/api/qa/pdf-question' && method === 'POST') {
    const body = await parseJSON(req);
    if (body === null) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ message: 'Bad Request' }));
      return;
    }

    const pdfText = body.pdfText || body.text || '';
    const question = body.question || '';

    if (!pdfText || !question) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ message: 'Bad Request' }));
      return;
    }

    // Return a simple echoed answer
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ answer: `ANSWER: ${question}` }));
    return;
  }

  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ message: 'Not Found' }));
});

server.listen(PORT, () => {
  console.log(`Mock server listening on http://localhost:${PORT}`);
});

// Graceful shutdown
process.on('SIGINT', () => server.close(() => process.exit(0)));
process.on('SIGTERM', () => server.close(() => process.exit(0)));