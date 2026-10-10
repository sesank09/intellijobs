const fs = require('fs');
const path = require('path');

// Load Jobs Data
let jobs = [];
try {
  const jsonPath = path.join(__dirname, '..', 'data', 'jobs.json');
  if (fs.existsSync(jsonPath)) {
    jobs = JSON.parse(fs.readFileSync(jsonPath, 'utf8'));
  }
} catch (e) {
  console.error('Error loading jobs.json:', e);
}

// Fallback if data/jobs.json wasn't loaded
if (!jobs || jobs.length === 0) {
  try {
    const csvPath = path.join(__dirname, '..', 'data', 'jobs.csv');
    const content = fs.readFileSync(csvPath, 'utf8');
    const lines = content.split(/\r?\n/).filter(l => l.trim().length > 0);
    for (let i = 1; i < lines.length; i++) {
      const line = lines[i];
      let fields = line.split('\t', 9);
      if (fields.length !== 9) fields = line.split(',', 9);
      if (fields.length === 9) {
        const id = parseInt(fields[0].trim(), 10);
        const title = fields[1].trim();
        const company = fields[2].trim();
        const location = fields[3].trim();
        const qualification = fields[4].trim();
        const experience = fields[5].trim();
        const salary = fields[6].trim();
        const skills = fields[7].trim();
        const description = fields[8].trim();
        const category = categoryFor(fields);
        const employmentType = experience.toLowerCase().includes('intern') ? 'Internship' : 'Full-time';
        const applyUrl = 'https://careers.' + company.toLowerCase().replace(/[^a-z0-9]/g, '') + '.com/jobs/' + id;
        const searchText = (title + ' ' + company + ' ' + location + ' ' + qualification + ' ' + experience + ' ' + salary + ' ' + skills + ' ' + description + ' ' + category).toLowerCase();
        jobs.push({ id, title, company, location, qualification, experience, salary, skills, description, category, employmentType, applyUrl, searchText });
      }
    }
  } catch (err) {
    console.error('Failed to load fallback CSV:', err);
  }
}

function categoryFor(fields) {
  const text = fields.join(' ').toLowerCase();
  if (/cyber|security|penetration|soc analyst|information security/i.test(text)) return 'Cybersecurity';
  if (/cloud|devops|aws|azure|docker|kubernetes|infrastructure/i.test(text)) return 'Cloud & DevOps';
  if (/data analyst|machine learning|data scientist|artificial intelligence|\bai\b|analytics|business intelligence|computer vision|nlp/i.test(text)) return 'Data & AI';
  if (/frontend|front-end|web developer|ui developer|react|html|css|javascript|full stack/i.test(text)) return 'Web Development';
  return 'Software';
}

function extractMaxSalary(salaryStr) {
  if (!salaryStr) return 0;
  const match = salaryStr.match(/(\d+)(?:-(\d+))?\s*LPA/i);
  if (match) {
    return match[2] ? parseInt(match[2], 10) : parseInt(match[1], 10);
  }
  return 0;
}

// -------------------------------------------------------------
// DSA ALGORITHMS IMPLEMENTATION
// -------------------------------------------------------------

// 1. Knuth-Morris-Pratt (KMP)
function computeLPS(pattern) {
  const m = pattern.length;
  const lps = new Array(m).fill(0);
  let len = 0;
  let i = 1;
  while (i < m) {
    if (pattern[i] === pattern[len]) {
      len++;
      lps[i] = len;
      i++;
    } else {
      if (len !== 0) {
        len = lps[len - 1];
      } else {
        lps[i] = 0;
        i++;
      }
    }
  }
  return lps;
}

function kmpSearch(text, pattern) {
  if (!text || !pattern) return -1;
  const lowerText = text.toLowerCase();
  const lowerPattern = pattern.toLowerCase();
  const n = lowerText.length;
  const m = lowerPattern.length;
  if (m === 0) return 0;
  if (m > n) return -1;

  const lps = computeLPS(lowerPattern);
  let i = 0;
  let j = 0;
  while (i < n) {
    if (lowerText[i] === lowerPattern[j]) {
      i++;
      j++;
    }
    if (j === m) {
      return i - j;
    } else if (i < n && lowerText[i] !== lowerPattern[j]) {
      if (j !== 0) {
        j = lps[j - 1];
      } else {
        i++;
      }
    }
  }
  return -1;
}

// 2. Rabin-Karp Rolling Hash
const RK_PRIME = 1000000007;
const RK_BASE = 256;

function rabinKarpSearch(text, pattern) {
  if (!text || !pattern) return -1;
  const lowerText = text.toLowerCase();
  const lowerPattern = pattern.toLowerCase();
  const n = lowerText.length;
  const m = lowerPattern.length;
  if (m === 0) return 0;
  if (m > n) return -1;

  let patternHash = 0;
  let textHash = 0;
  let h = 1;

  for (let i = 0; i < m - 1; i++) {
    h = (h * RK_BASE) % RK_PRIME;
  }

  for (let i = 0; i < m; i++) {
    patternHash = (RK_BASE * patternHash + lowerPattern.charCodeAt(i)) % RK_PRIME;
    textHash = (RK_BASE * textHash + lowerText.charCodeAt(i)) % RK_PRIME;
  }

  for (let i = 0; i <= n - m; i++) {
    if (patternHash === textHash) {
      let match = true;
      for (let j = 0; j < m; j++) {
        if (lowerText[i + j] !== lowerPattern[j]) {
          match = false;
          break;
        }
      }
      if (match) return i;
    }

    if (i < n - m) {
      textHash = (RK_BASE * (textHash - lowerText.charCodeAt(i) * h) + lowerText.charCodeAt(i + m)) % RK_PRIME;
      if (textHash < 0) textHash += RK_PRIME;
    }
  }
  return -1;
}

// 3. Z-Algorithm
function calculateZ(s) {
  const n = s.length;
  const z = new Array(n).fill(0);
  let l = 0, r = 0;
  for (let i = 1; i < n; i++) {
    if (i <= r) {
      z[i] = Math.min(r - i + 1, z[i - l]);
    }
    while (i + z[i] < n && s[z[i]] === s[i + z[i]]) {
      z[i]++;
    }
    if (i + z[i] - 1 > r) {
      l = i;
      r = i + z[i] - 1;
    }
  }
  return z;
}

function zAlgorithmSearch(text, pattern) {
  if (!text || !pattern) return -1;
  const lowerText = text.toLowerCase();
  const lowerPattern = pattern.toLowerCase();
  const n = lowerText.length;
  const m = lowerPattern.length;
  if (m === 0) return 0;
  if (m > n) return -1;

  const combined = lowerPattern + '\0' + lowerText;
  const z = calculateZ(combined);
  for (let i = m + 1; i < combined.length; i++) {
    if (z[i] >= m) {
      return i - m - 1;
    }
  }
  return -1;
}

// 4. Edit Distance (Levenshtein)
function editDistance(s1, s2) {
  if (s1 == null && s2 == null) return 0;
  if (s1 == null) return s2.length;
  if (s2 == null) return s1.length;
  const str1 = s1.toLowerCase().trim();
  const str2 = s2.toLowerCase().trim();
  const m = str1.length;
  const n = str2.length;
  const dp = Array.from({ length: m + 1 }, () => new Array(n + 1).fill(0));
  for (let i = 0; i <= m; i++) dp[i][0] = i;
  for (let j = 0; j <= n; j++) dp[0][j] = j;

  for (let i = 1; i <= m; i++) {
    for (let j = 1; j <= n; j++) {
      if (str1[i - 1] === str2[j - 1]) {
        dp[i][j] = dp[i - 1][j - 1];
      } else {
        dp[i][j] = 1 + Math.min(dp[i][j - 1], dp[i - 1][j], dp[i - 1][j - 1]);
      }
    }
  }
  return dp[m][n];
}

function fuzzyMatch(text, query, maxDistance = 2) {
  if (!text || !query || !query.trim()) return false;
  const q = query.trim().toLowerCase();
  const t = text.trim().toLowerCase();
  if (t.includes(q)) return true;

  const tokens = t.split(/[\s,;|./()\-]+/);
  const allowed = Math.max(1, maxDistance);
  for (const token of tokens) {
    const clean = token.trim();
    if (!clean) continue;
    const tokenThreshold = clean.length <= 3 ? 1 : allowed;
    if (editDistance(clean, q) <= tokenThreshold) return true;
  }

  if (q.length >= 4 && t.length >= q.length) {
    for (let i = 0; i <= t.length - q.length; i++) {
      const sub = t.substring(i, i + q.length);
      if (editDistance(sub, q) <= allowed) return true;
    }
  }
  return false;
}

// 5. Jaccard Similarity
function tokenizeSkills(skills) {
  if (!skills || !skills.trim()) return new Set();
  const set = new Set();
  const items = skills.split(/[,;|]/);
  for (const item of items) {
    const norm = item.trim().replace(/\s+/g, ' ').toLowerCase();
    if (norm) set.add(norm);
  }
  return set;
}

function jaccardSimilarity(skills1, skills2) {
  const set1 = tokenizeSkills(skills1);
  const set2 = tokenizeSkills(skills2);
  if (set1.size === 0 && set2.size === 0) return 1.0;
  if (set1.size === 0 || set2.size === 0) return 0.0;
  let intersection = 0;
  for (const s of set1) {
    if (set2.has(s)) intersection++;
  }
  const union = set1.size + set2.size - intersection;
  return union === 0 ? 0.0 : intersection / union;
}

// 6. Graph Helper Class
class Graph {
  constructor(vertices) {
    this.vertices = vertices;
    this.adjacency = Array.from({ length: vertices }, () => new Array(vertices).fill(0));
    this.vertexLabels = {};
  }
  setVertexLabel(v, label) {
    this.vertexLabels[v] = label;
  }
  getVertexLabel(v) {
    return this.vertexLabels[v] || `Vertex ${v}`;
  }
  addEdge(src, dest) {
    this.adjacency[src][dest] = 1;
  }
  addWeightedUndirectedEdge(u, v, weight) {
    this.adjacency[u][v] = weight;
    this.adjacency[v][u] = weight;
  }
}

function buildSkillPathwayGraph() {
  const g = new Graph(8);
  g.setVertexLabel(0, "Programming Foundations");
  g.setVertexLabel(1, "OOP & Data Structures");
  g.setVertexLabel(2, "Web & REST APIs");
  g.setVertexLabel(3, "Relational Databases & SQL");
  g.setVertexLabel(4, "Spring Boot Backend");
  g.setVertexLabel(5, "Frontend Architecture");
  g.setVertexLabel(6, "Cloud & DevOps (Docker/AWS)");
  g.setVertexLabel(7, "Enterprise Microservices");

  g.addEdge(0, 1);
  g.addEdge(0, 2);
  g.addEdge(1, 3);
  g.addEdge(2, 4);
  g.addEdge(2, 5);
  g.addEdge(3, 4);
  g.addEdge(4, 6);
  g.addEdge(4, 7);
  g.addEdge(6, 7);
  return g;
}

function buildSpecializationGraph() {
  const g = new Graph(8);
  g.setVertexLabel(0, "Mathematics & Statistics");
  g.setVertexLabel(1, "Data Analysis & SQL");
  g.setVertexLabel(2, "Machine Learning Core");
  g.setVertexLabel(3, "Deep Learning Foundations");
  g.setVertexLabel(4, "Computer Vision (CNN/OpenCV)");
  g.setVertexLabel(5, "NLP & Text Processing");
  g.setVertexLabel(6, "Large Language Models & GenAI");
  g.setVertexLabel(7, "Production MLOps");

  g.addEdge(0, 1);
  g.addEdge(1, 2);
  g.addEdge(2, 3);
  g.addEdge(3, 4);
  g.addEdge(3, 5);
  g.addEdge(5, 6);
  g.addEdge(6, 7);
  return g;
}

function buildLearningRoadmapGraph() {
  const g = new Graph(7);
  g.setVertexLabel(0, "Junior Developer");
  g.setVertexLabel(1, "Backend API Developer");
  g.setVertexLabel(2, "Data Associate");
  g.setVertexLabel(3, "Machine Learning Engineer");
  g.setVertexLabel(4, "Full Stack Engineer");
  g.setVertexLabel(5, "DevOps Specialist");
  g.setVertexLabel(6, "AI & Cloud Architect");

  g.addWeightedUndirectedEdge(0, 1, 4);
  g.addWeightedUndirectedEdge(0, 2, 3);
  g.addWeightedUndirectedEdge(1, 3, 6);
  g.addWeightedUndirectedEdge(2, 3, 5);
  g.addWeightedUndirectedEdge(1, 4, 4);
  g.addWeightedUndirectedEdge(1, 5, 5);
  g.addWeightedUndirectedEdge(4, 5, 3);
  g.addWeightedUndirectedEdge(3, 6, 7);
  g.addWeightedUndirectedEdge(5, 6, 6);
  return g;
}

// BFS
function runBFS(graph, start) {
  const n = graph.vertices;
  const adj = graph.adjacency;
  const visited = new Array(n).fill(false);
  const queue = [];
  const order = [];
  const orderLabels = [];
  const levels = {};
  const steps = [];

  visited[start] = true;
  levels[start] = 0;
  queue.push(start);

  while (queue.length > 0) {
    const current = queue.shift();
    order.push(current);
    orderLabels.push(graph.getVertexLabel(current));
    const currentLevel = levels[current];

    for (let neighbor = 0; neighbor < n; neighbor++) {
      if (adj[current][neighbor] !== 0 && !visited[neighbor]) {
        visited[neighbor] = true;
        levels[neighbor] = currentLevel + 1;
        queue.push(neighbor);
      }
    }
    steps.push({
      current,
      label: graph.getVertexLabel(current),
      level: currentLevel,
      queueState: [...queue]
    });
  }

  return {
    order,
    orderLabels,
    levels,
    steps,
    explanation: "Breadth-First Search (BFS) explores all neighboring skill competencies at level k before proceeding to level k+1. In career pathways, this models foundational breadth-first curriculum progression."
  };
}

// DFS
function runDFS(graph, start) {
  const n = graph.vertices;
  const visited = new Array(n).fill(false);
  const order = [];
  const orderLabels = [];
  const steps = [];

  function dfsRecursive(current, depth) {
    visited[current] = true;
    order.push(current);
    orderLabels.push(graph.getVertexLabel(current));
    steps.push({ vertex: current, label: graph.getVertexLabel(current), depth, action: "VISIT" });

    for (let neighbor = 0; neighbor < n; neighbor++) {
      if (graph.adjacency[current][neighbor] !== 0 && !visited[neighbor]) {
        dfsRecursive(neighbor, depth + 1);
        steps.push({ vertex: current, label: graph.getVertexLabel(current), depth, action: "BACKTRACK_TO" });
      }
    }
  }

  dfsRecursive(start, 0);

  return {
    order,
    orderLabels,
    steps,
    explanation: "Depth-First Search (DFS) explores vertical specialization paths to their maximum depth before backtracking. In recruitment, this models deep vertical technical specialization pathways."
  };
}

// Dijkstra
function runDijkstra(graph, source, target) {
  const n = graph.vertices;
  const INF = 1000000000;
  const dist = new Array(n).fill(INF);
  const parent = new Array(n).fill(-1);
  const visited = new Array(n).fill(false);
  const relaxationLog = [];

  dist[source] = 0;

  for (let count = 0; count < n; count++) {
    let u = -1;
    for (let i = 0; i < n; i++) {
      if (!visited[i] && (u === -1 || dist[i] < dist[u])) {
        u = i;
      }
    }
    if (u === -1 || dist[u] === INF) break;
    visited[u] = true;

    for (let v = 0; v < n; v++) {
      const weight = graph.adjacency[u][v];
      if (!visited[v] && weight > 0 && dist[u] + weight < dist[v]) {
        const oldDist = dist[v];
        dist[v] = dist[u] + weight;
        parent[v] = u;
        relaxationLog.push(
          `Relaxed edge (${graph.getVertexLabel(u)} -> ${graph.getVertexLabel(v)}): updated distance from ${oldDist === INF ? 'INF' : oldDist} to ${dist[v]} weeks (weight: ${weight})`
        );
      }
    }
  }

  const path = [];
  const pathLabels = [];
  if (dist[target] !== INF) {
    let curr = target;
    while (curr !== -1) {
      path.unshift(curr);
      curr = parent[curr];
    }
    for (const node of path) {
      pathLabels.push(graph.getVertexLabel(node));
    }
  }

  const allDistances = {};
  for (let i = 0; i < n; i++) {
    allDistances[i] = dist[i] === INF ? -1 : dist[i];
  }

  return {
    source,
    target,
    totalDistance: dist[target] === INF ? -1 : dist[target],
    path,
    pathLabels,
    allDistances,
    relaxationLog,
    explanation: "Dijkstra's Algorithm finds the shortest path by greedily choosing the unvisited node with the smallest known distance and relaxing all outgoing edges. Models minimum total learning effort (weeks) required to transition between careers."
  };
}

// Bipartite Matching
function runBipartiteMatching() {
  const candidates = [
    { name: "Alice Sharma", skills: ["Java", "Spring Boot", "SQL"] },
    { name: "Bob Rao", skills: ["Python", "Machine Learning", "Statistics"] },
    { name: "Charlie Verma", skills: ["HTML", "CSS", "JavaScript", "React"] },
    { name: "Diana Nair", skills: ["AWS", "Linux", "Docker", "CI/CD"] },
    { name: "Ethan Gupta", skills: ["Java", "React", "SQL", "Spring Boot"] }
  ];

  const targetJobs = [
    jobs.find(j => j.id === 101) || jobs[0],
    jobs.find(j => j.id === 106) || jobs[5],
    jobs.find(j => j.id === 103) || jobs[2],
    jobs.find(j => j.id === 112) || jobs[11],
    jobs.find(j => j.id === 109) || jobs[8]
  ];

  const leftCount = candidates.length;
  const rightCount = targetJobs.length;
  const edges = Array.from({ length: leftCount }, () => new Array(rightCount).fill(0));
  const edgeList = [];

  for (let c = 0; c < leftCount; c++) {
    const cand = candidates[c];
    for (let j = 0; j < rightCount; j++) {
      const jb = targetJobs[j];
      const sim = jaccardSimilarity(cand.skills.join(', '), jb.skills);
      if (sim >= 0.25) {
        edges[c][j] = 1;
        edgeList.push({
          candidateIndex: c,
          candidateName: cand.name,
          jobIndex: j,
          jobTitle: jb.title,
          similarity: Math.round(sim * 100.0)
        });
      }
    }
  }

  const matchedLeftForRight = new Array(rightCount).fill(-1);
  const visitedRight = new Array(rightCount).fill(false);
  const augmentingPathLog = [];

  function tryMatch(candidate) {
    for (let job = 0; job < rightCount; job++) {
      if (edges[candidate][job] === 1 && !visitedRight[job]) {
        visitedRight[job] = true;
        if (matchedLeftForRight[job] === -1) {
          matchedLeftForRight[job] = candidate;
          augmentingPathLog.push(`Augmenting Path: Assigned Candidate #${candidate} to Job #${job}`);
          return true;
        } else if (tryMatch(matchedLeftForRight[job])) {
          augmentingPathLog.push(`Reassigned Job #${job} to Candidate #${candidate} after finding alternative for previous holder`);
          matchedLeftForRight[job] = candidate;
          return true;
        }
      }
    }
    return false;
  }

  let totalMatches = 0;
  for (let c = 0; c < leftCount; c++) {
    visitedRight.fill(false);
    if (tryMatch(c)) {
      totalMatches++;
    }
  }

  const assignments = [];
  const candidateMatched = new Array(leftCount).fill(false);

  for (let j = 0; j < rightCount; j++) {
    const c = matchedLeftForRight[j];
    if (c !== -1) {
      candidateMatched[c] = true;
      const cand = candidates[c];
      const jb = targetJobs[j];
      assignments.push({
        candidateIndex: c,
        candidateName: cand.name,
        candidateSkills: cand.skills,
        jobIndex: j,
        jobId: jb.id,
        jobTitle: jb.title,
        company: jb.company,
        jobSkills: jb.skills
      });
    }
  }

  const unmatched = [];
  for (let c = 0; c < leftCount; c++) {
    if (!candidateMatched[c]) unmatched.push(candidates[c].name);
  }

  return {
    algorithm: "Maximum Bipartite Matching",
    scenario: "Optimal Candidate-to-Job Role Allocation (Augmenting Paths)",
    maxMatches: totalMatches,
    totalCandidates: candidates.length,
    totalJobs: targetJobs.length,
    assignments,
    unmatchedCandidates: unmatched,
    compatibilityEdges: edgeList,
    augmentingPathLog,
    candidates,
    targetJobs,
    explanation: "Augmenting Path algorithm maximizes the number of placed candidates by finding alternating augmenting paths and reassigning matching edges.",
    timeComplexity: "O(V * E)",
    spaceComplexity: "O(V)"
  };
}

// -------------------------------------------------------------
// REQUEST HANDLER (VERCEL SERVERLESS FUNCTION)
// -------------------------------------------------------------
async function parseBody(req) {
  if (req.body && typeof req.body === 'object') return req.body;
  if (typeof req.body === 'string') {
    try { return JSON.parse(req.body); } catch (e) { return {}; }
  }
  return new Promise((resolve) => {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      try {
        resolve(JSON.parse(body || '{}'));
      } catch (e) {
        resolve({});
      }
    });
  });
}

module.exports = async (req, res) => {
  // CORS Headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.statusCode = 200;
    return res.end();
  }

  // Support express-like helper methods if not present
  if (!res.status) {
    res.status = function(code) {
      res.statusCode = code;
      return res;
    };
  }
  if (!res.json) {
    res.json = function(data) {
      res.setHeader('Content-Type', 'application/json');
      res.end(JSON.stringify(data));
      return res;
    };
  }

  const urlObj = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = urlObj.pathname;
  const searchParams = urlObj.searchParams;

  // ROUTE 1: GET /api/stats
  if (pathname === '/api/stats' && req.method === 'GET') {
    const categoryCounts = {};
    const locationCounts = {};
    for (const job of jobs) {
      categoryCounts[job.category] = (categoryCounts[job.category] || 0) + 1;
      locationCounts[job.location] = (locationCounts[job.location] || 0) + 1;
    }
    return res.status(200).json({
      totalJobs: jobs.length,
      categories: categoryCounts,
      locations: locationCounts,
      supportedAlgorithms: ["KMP", "Rabin-Karp", "Z-Algorithm", "Edit Distance"],
      supportedGraphAlgorithms: ["BFS", "DFS", "Dijkstra", "Bipartite Matching"]
    });
  }

  // ROUTE 2: GET /api/jobs/:id
  const jobByIdMatch = pathname.match(/^\/api\/jobs\/(\d+)$/);
  if (jobByIdMatch && req.method === 'GET') {
    const id = parseInt(jobByIdMatch[1], 10);
    const job = jobs.find(j => j.id === id);
    if (!job) {
      return res.status(404).json({ error: "Job not found" });
    }
    return res.status(200).json(job);
  }

  // ROUTE 3: GET /api/jobs
  if (pathname === '/api/jobs' && req.method === 'GET') {
    const keyword = searchParams.get('keyword');
    const algorithm = (searchParams.get('algorithm') || 'kmp').toLowerCase().replace('-', '');
    const location = searchParams.get('location');
    const category = searchParams.get('category');
    const sort = (searchParams.get('sort') || 'recommended').toLowerCase();

    let results = [];

    for (const job of jobs) {
      let matchesKeyword = true;
      if (keyword && keyword.trim().length > 0) {
        const query = keyword.trim();
        const text = job.searchText;

        switch (algorithm) {
          case 'rabinkarp':
            matchesKeyword = rabinKarpSearch(text, query) !== -1;
            break;
          case 'zalgorithm':
          case 'z':
            matchesKeyword = zAlgorithmSearch(text, query) !== -1;
            break;
          case 'editdistance':
          case 'fuzzy':
            matchesKeyword = fuzzyMatch(text, query, 2);
            break;
          case 'kmp':
          default:
            matchesKeyword = kmpSearch(text, query) !== -1;
            break;
        }
      }

      if (!matchesKeyword) continue;

      if (location && location.trim() && location.toLowerCase() !== 'all') {
        if (!job.location.toLowerCase().includes(location.trim().toLowerCase())) {
          continue;
        }
      }

      if (category && category.trim() && category.toLowerCase() !== 'all') {
        if (job.category.toLowerCase() !== category.trim().toLowerCase()) {
          continue;
        }
      }

      results.push(job);
    }

    if (sort === 'salary') {
      results.sort((a, b) => extractMaxSalary(b.salary) - extractMaxSalary(a.salary));
    } else if (sort === 'title') {
      results.sort((a, b) => a.title.localeCompare(b.title));
    }

    return res.status(200).json(results);
  }

  // ROUTE 4: POST /api/jobs/match
  if (pathname === '/api/jobs/match' && req.method === 'POST') {
    const body = await parseBody(req);
    const candidateSkills = body.skills;
    if (!candidateSkills || !candidateSkills.trim()) {
      return res.status(400).json({ error: "Enter at least one skill." });
    }

    const candidateTokens = tokenizeSkills(candidateSkills);
    if (candidateTokens.size === 0) {
      return res.status(200).json([]);
    }

    const matches = [];
    for (const job of jobs) {
      const requiredTokens = tokenizeSkills(job.skills);
      const matchedSkills = [];
      const missingSkills = [];

      for (const cand of candidateTokens) {
        if (requiredTokens.has(cand)) matchedSkills.push(cand);
      }
      for (const req of requiredTokens) {
        if (!candidateTokens.has(req)) missingSkills.push(req);
      }

      const union = candidateTokens.size + requiredTokens.size - matchedSkills.length;
      const score = union === 0 ? 0.0 : matchedSkills.length / union;

      matches.push({
        job,
        score,
        matchedSkills,
        missingSkills
      });
    }

    matches.sort((a, b) => {
      if (b.score !== a.score) return b.score - a.score;
      return a.job.title.localeCompare(b.job.title);
    });

    return res.status(200).json(matches);
  }

  // ROUTE 5: POST /api/algorithms/search
  if (pathname === '/api/algorithms/search' && req.method === 'POST') {
    const body = await parseBody(req);
    const pattern = body.pattern || body.query;
    if (!pattern || !pattern.trim()) {
      return res.status(400).json({ error: "Search pattern cannot be empty." });
    }

    const query = pattern.trim();
    const algorithm = (body.algorithm || 'kmp').toLowerCase().replace('-', '');
    const threshold = body.threshold && body.threshold > 0 ? body.threshold : 2;

    const matchedJobs = [];
    const startTimeHr = process.hrtime.bigint();

    for (const job of jobs) {
      const text = job.searchText;
      let match = false;
      switch (algorithm) {
        case 'rabinkarp':
          match = rabinKarpSearch(text, query) !== -1;
          break;
        case 'zalgorithm':
        case 'z':
          match = zAlgorithmSearch(text, query) !== -1;
          break;
        case 'editdistance':
        case 'fuzzy':
          match = fuzzyMatch(text, query, threshold);
          break;
        case 'kmp':
        default:
          match = kmpSearch(text, query) !== -1;
          break;
      }
      if (match) matchedJobs.push(job);
    }

    const durationNano = Number(process.hrtime.bigint() - startTimeHr);
    const durationMs = Math.round((durationNano / 1_000_000) * 1000) / 1000;

    let algoName, timeComplexity, spaceComplexity, explanation;
    switch (algorithm) {
      case 'rabinkarp':
        algoName = "Rabin-Karp Rolling Hash";
        timeComplexity = "O(N + M) average, O(N * M) worst-case";
        spaceComplexity = "O(1)";
        explanation = "Rabin-Karp slides an O(1) rolling hash window over the text. When the rolling hash matches patternHash, it performs character verification to eliminate collisions.";
        break;
      case 'zalgorithm':
      case 'z':
        algoName = "Z-Algorithm (Z-Array)";
        timeComplexity = "O(N + M)";
        spaceComplexity = "O(N + M)";
        explanation = "Z-Algorithm constructs the string S = Pattern + '$' + Text and builds the Z-array where Z[i] represents the length of the longest substring starting at i matching pattern prefix.";
        break;
      case 'editdistance':
      case 'fuzzy':
        algoName = "Edit Distance (Levenshtein DP)";
        timeComplexity = "O(N * M)";
        spaceComplexity = "O(N * M)";
        explanation = "Edit Distance calculates the minimum insertions, deletions, and substitutions to transform query into token via Dynamic Programming table. Ideal for handling candidate typos.";
        break;
      case 'kmp':
      default:
        algoName = "Knuth-Morris-Pratt (KMP)";
        timeComplexity = "O(N + M)";
        spaceComplexity = "O(M)";
        explanation = "KMP preprocesses the pattern to construct the Longest Prefix Suffix (LPS) table, skipping redundant comparisons upon mismatches without ever backtracking the text pointer.";
        break;
    }

    return res.status(200).json({
      algorithm: algoName,
      algorithmKey: algorithm,
      pattern: query,
      recordsProcessed: jobs.length,
      matchesFound: matchedJobs.length,
      executionTimeNano: durationNano,
      executionTimeMs: durationMs,
      timeComplexity,
      spaceComplexity,
      explanation,
      matchedJobs
    });
  }

  // ROUTE 6: POST /api/algorithms/compare
  if (pathname === '/api/algorithms/compare' && req.method === 'POST') {
    const body = await parseBody(req);
    const pattern = body.pattern || body.query;
    if (!pattern || !pattern.trim()) {
      return res.status(400).json({ error: "Search pattern cannot be empty." });
    }

    const query = pattern.trim();
    const benchmarks = [];

    // Helper for entry
    function makeEntry(name, key, matches, nano, timeComp, spaceComp, notes) {
      return {
        name,
        key,
        matchesFound: matches,
        timeNano: nano,
        timeMs: Math.round((nano / 1_000_000.0) * 1000.0) / 1000.0,
        timeMicro: Math.round((nano / 1_000.0) * 10.0) / 10.0,
        timeComplexity: timeComp,
        spaceComplexity: spaceComp,
        notes
      };
    }

    // 1. KMP
    let t0 = process.hrtime.bigint();
    let kmpMatches = 0;
    for (const job of jobs) {
      if (kmpSearch(job.searchText, query) !== -1) kmpMatches++;
    }
    let kmpNano = Number(process.hrtime.bigint() - t0);
    benchmarks.push(makeEntry("Knuth-Morris-Pratt (KMP)", "kmp", kmpMatches, kmpNano, "O(N + M)", "O(M)", "Precomputes LPS table; zero backtracking on text pointer. Ideal for fixed pattern search."));

    // 2. Rabin-Karp
    t0 = process.hrtime.bigint();
    let rkMatches = 0;
    for (const job of jobs) {
      if (rabinKarpSearch(job.searchText, query) !== -1) rkMatches++;
    }
    let rkNano = Number(process.hrtime.bigint() - t0);
    benchmarks.push(makeEntry("Rabin-Karp", "rabinkarp", rkMatches, rkNano, "O(N + M) avg", "O(1)", "Polynomial rolling hash with character collision check. Excellent for multi-pattern hashing."));

    // 3. Z-Algorithm
    t0 = process.hrtime.bigint();
    let zMatches = 0;
    for (const job of jobs) {
      if (zAlgorithmSearch(job.searchText, query) !== -1) zMatches++;
    }
    let zNano = Number(process.hrtime.bigint() - t0);
    benchmarks.push(makeEntry("Z-Algorithm", "zalgorithm", zMatches, zNano, "O(N + M)", "O(N + M)", "Z-box prefix matching over concatenated string. Optimal linear pattern occurrence detection."));

    // 4. Edit Distance
    t0 = process.hrtime.bigint();
    let edMatches = 0;
    for (const job of jobs) {
      if (fuzzyMatch(job.searchText, query, 2)) edMatches++;
    }
    let edNano = Number(process.hrtime.bigint() - t0);
    benchmarks.push(makeEntry("Edit Distance (Levenshtein)", "editdistance", edMatches, edNano, "O(N * M)", "O(N * M)", "Dynamic programming recurrence. Tolerates typos and approximate candidate input."));

    return res.status(200).json({
      pattern: query,
      recordsProcessed: jobs.length,
      benchmarks,
      note: "Execution times measured with high-resolution Node.js / Vercel Serverless process.hrtime.bigint() precision."
    });
  }

  // ROUTE 7: GET /api/graph/demo
  if (pathname === '/api/graph/demo' && req.method === 'GET') {
    const type = (searchParams.get('type') || 'bfs').toLowerCase().trim();
    const sourceParam = searchParams.get('source');
    const targetParam = searchParams.get('target');
    const source = sourceParam !== null ? parseInt(sourceParam, 10) : null;
    const target = targetParam !== null ? parseInt(targetParam, 10) : null;

    if (type === 'bfs') {
      const graph = buildSkillPathwayGraph();
      const src = (source !== null && source >= 0 && source < graph.vertices) ? source : 0;
      const result = runBFS(graph, src);
      return res.status(200).json({
        algorithm: "Breadth-First Search (BFS)",
        scenario: "Skill Prerequisite & Curriculum Pathway Traversal",
        startVertex: src,
        startLabel: graph.getVertexLabel(src),
        traversalOrder: result.order,
        traversalLabels: result.orderLabels,
        levels: result.levels,
        steps: result.steps,
        vertexLabels: graph.vertexLabels,
        adjacencyMatrix: graph.adjacency,
        explanation: result.explanation,
        timeComplexity: "O(V + E)",
        spaceComplexity: "O(V)"
      });
    }

    if (type === 'dfs') {
      const graph = buildSpecializationGraph();
      const src = (source !== null && source >= 0 && source < graph.vertices) ? source : 0;
      const result = runDFS(graph, src);
      return res.status(200).json({
        algorithm: "Depth-First Search (DFS)",
        scenario: "AI Domain Mastery & Deep Vertical Specialization",
        startVertex: src,
        startLabel: graph.getVertexLabel(src),
        traversalOrder: result.order,
        traversalLabels: result.orderLabels,
        steps: result.steps,
        vertexLabels: graph.vertexLabels,
        adjacencyMatrix: graph.adjacency,
        explanation: result.explanation,
        timeComplexity: "O(V + E)",
        spaceComplexity: "O(V)"
      });
    }

    if (type === 'dijkstra') {
      const graph = buildLearningRoadmapGraph();
      const src = (source !== null && source >= 0 && source < graph.vertices) ? source : 0;
      const tgt = (target !== null && target >= 0 && target < graph.vertices) ? target : 6;
      const result = runDijkstra(graph, src, tgt);
      return res.status(200).json({
        algorithm: "Dijkstra's Shortest Path Algorithm",
        scenario: "Optimal Skill Bridge (Minimum Effort in Learning Weeks)",
        sourceVertex: src,
        targetVertex: tgt,
        sourceLabel: graph.getVertexLabel(src),
        targetLabel: graph.getVertexLabel(tgt),
        totalEffortWeeks: result.totalDistance,
        shortestPath: result.path,
        pathLabels: result.pathLabels,
        allDistances: result.allDistances,
        relaxationLog: result.relaxationLog,
        vertexLabels: graph.vertexLabels,
        adjacencyMatrix: graph.adjacency,
        explanation: result.explanation,
        timeComplexity: "O(V^2) or O((V + E) log V)",
        spaceComplexity: "O(V)"
      });
    }

    if (type === 'bipartite') {
      const result = runBipartiteMatching();
      return res.status(200).json(result);
    }

    return res.status(400).json({ error: "Unknown graph demo algorithm: " + type });
  }

  // Not found
  return res.status(404).json({ error: "Endpoint not found: " + pathname });
};

// Local standalone test server if run directly via `node api/index.js`
if (require.main === module) {
  const http = require('http');
  const server = http.createServer((req, res) => {
    module.exports(req, res);
  });
  const PORT = process.env.PORT || 3000;
  server.listen(PORT, () => {
    console.log(`IntelliJobs API server running at http://localhost:${PORT}`);
  });
}
