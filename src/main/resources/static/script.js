/**
 * INTELLIGENT JOB PORTAL — CLIENT SCRIPT
 * DSA-3 Academic Project
 * Connects directly to Java Spring Boot REST APIs via vanilla fetch()
 */

// Application State
const AppState = {
    allJobs: [],
    savedJobs: new Set(JSON.parse(localStorage.getItem('intellijobs_saved_ids') || '[]')),
    currentCategory: 'All',
    selectedAlgorithm: 'kmp',
    selectedSort: 'recommended',
    currentLocation: 'All',
    activeGraphDemo: 'bfs'
};

// Algorithm Display Names
const ALGO_NAMES = {
    kmp: 'Knuth-Morris-Pratt (KMP)',
    rabinkarp: 'Rabin-Karp Rolling Hash',
    zalgorithm: 'Z-Algorithm',
    editdistance: 'Edit Distance (Fuzzy Levenshtein)'
};

// DOM Content Loaded Handler
document.addEventListener('DOMContentLoaded', () => {
    initNavigation();
    initHeroSearch();
    initJobFilters();
    initSkillMatcher();
    initAlgorithmLab();
    initSavedJobs();
    initModals();

    // Initial API loads
    loadInitialJobs();
    loadStats();
    loadGraphDemo('bfs');
});

/* ==========================================================================
   NAVIGATION & UI CONTROLS
   ========================================================================== */
function initNavigation() {
    const navLinks = document.querySelectorAll('.main-nav .nav-link');
    const mobileToggle = document.getElementById('mobile-menu-toggle');
    const mainNav = document.getElementById('main-nav');

    // Smooth navigation active indicator
    window.addEventListener('scroll', () => {
        const sections = ['home', 'jobs', 'skill-matcher', 'algorithm-lab', 'about-project'];
        const scrollPos = window.scrollY + 120;

        for (const sectionId of sections) {
            const section = document.getElementById(sectionId);
            if (section) {
                const top = section.offsetTop;
                const height = section.offsetHeight;
                if (scrollPos >= top && scrollPos < top + height) {
                    navLinks.forEach(link => {
                        link.classList.toggle('active', link.getAttribute('href') === `#${sectionId}`);
                    });
                    break;
                }
            }
        }
    });

    // Mobile menu toggle
    if (mobileToggle && mainNav) {
        mobileToggle.addEventListener('click', () => {
            const isOpen = mainNav.classList.toggle('open');
            mobileToggle.setAttribute('aria-expanded', isOpen);
            mobileToggle.textContent = isOpen ? '✕' : '☰';
        });

        navLinks.forEach(link => {
            link.addEventListener('click', () => {
                mainNav.classList.remove('open');
                mobileToggle.setAttribute('aria-expanded', 'false');
                mobileToggle.textContent = '☰';
            });
        });
    }

    // Home category chips
    const homeCatChips = document.querySelectorAll('#home-category-chips .cat-chip');
    homeCatChips.forEach(chip => {
        chip.addEventListener('click', () => {
            homeCatChips.forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            const category = chip.dataset.category;

            // Sync with Jobs section
            AppState.currentCategory = category;
            const filterTabs = document.querySelectorAll('#category-tabs-list .tab-btn');
            filterTabs.forEach(t => t.classList.toggle('active', t.dataset.category === category));

            // Scroll to jobs and filter
            document.getElementById('jobs').scrollIntoView({ behavior: 'smooth' });
            fetchAndRenderJobs();
        });
    });
}

function initHeroSearch() {
    const heroBtn = document.getElementById('hero-search-btn');
    const heroKeyword = document.getElementById('hero-keyword-input');
    const heroLocation = document.getElementById('hero-location-input');

    if (heroBtn) {
        heroBtn.addEventListener('click', () => {
            const keyword = heroKeyword.value.trim();
            const location = heroLocation.value.trim();

            document.getElementById('job-keyword-input').value = keyword;
            if (location) {
                // If location matches a dropdown option, select it; else custom
                const locSelect = document.getElementById('job-location-select');
                let found = false;
                for (const opt of locSelect.options) {
                    if (opt.value.toLowerCase() === location.toLowerCase()) {
                        locSelect.value = opt.value;
                        found = true;
                        break;
                    }
                }
                if (!found && location.length > 0) {
                    AppState.currentLocation = location;
                }
            }

            document.getElementById('jobs').scrollIntoView({ behavior: 'smooth' });
            fetchAndRenderJobs();
        });

        // Trigger on Enter
        [heroKeyword, heroLocation].forEach(input => {
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') heroBtn.click();
            });
        });
    }
}

/* ==========================================================================
   JOB LISTINGS & FILTERING (BACKEND REST API)
   ========================================================================== */
function initJobFilters() {
    const keywordInput = document.getElementById('job-keyword-input');
    const locationSelect = document.getElementById('job-location-select');
    const algorithmSelect = document.getElementById('job-algorithm-select');
    const sortSelect = document.getElementById('job-sort-select');
    const resetBtn = document.getElementById('reset-filters-btn');
    const emptyResetBtn = document.getElementById('empty-state-reset-btn');
    const categoryTabs = document.querySelectorAll('#category-tabs-list .tab-btn');

    let debounceTimer = null;
    keywordInput.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            fetchAndRenderJobs();
        }, 250);
    });

    locationSelect.addEventListener('change', () => {
        AppState.currentLocation = locationSelect.value;
        fetchAndRenderJobs();
    });

    algorithmSelect.addEventListener('change', () => {
        AppState.selectedAlgorithm = algorithmSelect.value;
        showToast(`Switched algorithm to ${ALGO_NAMES[algorithmSelect.value]}`);
        fetchAndRenderJobs();
    });

    sortSelect.addEventListener('change', () => {
        AppState.selectedSort = sortSelect.value;
        fetchAndRenderJobs();
    });

    categoryTabs.forEach(tab => {
        tab.addEventListener('click', () => {
            categoryTabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            AppState.currentCategory = tab.dataset.category;
            fetchAndRenderJobs();
        });
    });

    const resetAll = () => {
        keywordInput.value = '';
        locationSelect.value = 'All';
        algorithmSelect.value = 'kmp';
        sortSelect.value = 'recommended';
        AppState.currentCategory = 'All';
        AppState.selectedAlgorithm = 'kmp';
        AppState.selectedSort = 'recommended';
        AppState.currentLocation = 'All';

        categoryTabs.forEach(t => t.classList.toggle('active', t.dataset.category === 'All'));
        document.querySelectorAll('#home-category-chips .cat-chip').forEach(c => c.classList.toggle('active', c.dataset.category === 'All'));

        fetchAndRenderJobs();
        showToast('All search filters reset to defaults.');
    };

    if (resetBtn) resetBtn.addEventListener('click', resetAll);
    if (emptyResetBtn) emptyResetBtn.addEventListener('click', resetAll);
}

async function loadInitialJobs() {
    await fetchAndRenderJobs();
}

async function fetchAndRenderJobs() {
    const container = document.getElementById('job-cards-container');
    const loadingSpinner = document.getElementById('jobs-loading-spinner');
    const emptyState = document.getElementById('jobs-empty-state');
    const statusText = document.getElementById('algo-status-text');
    const activeAlgoName = document.getElementById('active-algo-name');

    const keyword = (document.getElementById('job-keyword-input')?.value || '').trim();
    const location = AppState.currentLocation || 'All';
    const algorithm = AppState.selectedAlgorithm || 'kmp';
    const category = AppState.currentCategory || 'All';
    const sort = AppState.selectedSort || 'recommended';

    // Construct query parameters
    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (location && location !== 'All') params.append('location', location);
    if (algorithm) params.append('algorithm', algorithm);
    if (category && category !== 'All') params.append('category', category);
    if (sort) params.append('sort', sort);

    loadingSpinner.hidden = false;
    container.innerHTML = '';
    emptyState.hidden = true;

    activeAlgoName.textContent = ALGO_NAMES[algorithm] || algorithm.toUpperCase();

    const startTime = performance.now();

    try {
        const response = await fetch(`/api/jobs?${params.toString()}`);
        if (!response.ok) {
            throw new Error(`Server responded with ${response.status}`);
        }

        const jobs = await response.json();
        const duration = (performance.now() - startTime).toFixed(1);

        loadingSpinner.hidden = true;

        if (jobs.length === 0) {
            emptyState.hidden = false;
            statusText.textContent = `0 results found for "${keyword || 'selected filters'}" (${duration} ms)`;
        } else {
            emptyState.hidden = true;
            statusText.innerHTML = `Found <strong>${jobs.length}</strong> matching jobs using <strong>${ALGO_NAMES[algorithm]}</strong> in <strong>${duration} ms</strong>.`;
            renderJobCards(jobs, container);

            // Also populate featured cards on initial load
            if (AppState.allJobs.length === 0) {
                AppState.allJobs = jobs;
                renderFeaturedJobs(jobs.slice(0, 3));
            }
        }
    } catch (error) {
        console.error('Error fetching jobs:', error);
        loadingSpinner.hidden = true;
        statusText.textContent = `Error connecting to backend: ${error.message}`;
        showToast('Backend connection failed. Ensure Spring Boot is running on localhost:8080.');
    }
}

function renderJobCards(jobs, container) {
    container.innerHTML = jobs.map(job => {
        const isSaved = AppState.savedJobs.has(job.id);
        const skillsList = (job.skills || '').split(',').map(s => s.trim()).filter(Boolean);

        return `
            <article class="job-card" data-job-id="${job.id}">
                <div>
                    <div class="job-card-top">
                        <div class="company-logo-pill">${escapeHtml(job.company.charAt(0))}</div>
                        <button class="job-save-btn ${isSaved ? 'saved' : ''}" 
                                data-id="${job.id}" 
                                title="${isSaved ? 'Remove from saved' : 'Save job'}"
                                aria-label="Save ${escapeHtml(job.title)}">
                            ${isSaved ? '★' : '☆'}
                        </button>
                    </div>

                    <h3 class="job-role-title">${escapeHtml(job.title)}</h3>
                    <span class="job-company-name">${escapeHtml(job.company)}</span>

                    <div class="job-tags-row">
                        <span class="meta-tag">📍 ${escapeHtml(job.location)}</span>
                        <span class="meta-tag salary-tag">₹ ${escapeHtml(job.salary)}</span>
                        <span class="meta-tag">⏱ ${escapeHtml(job.experience)}</span>
                        <span class="meta-tag type-tag">${escapeHtml(job.employmentType || 'Full-time')}</span>
                        <span class="meta-tag category-tag">${escapeHtml(job.category)}</span>
                    </div>

                    <p class="job-description-snippet">${escapeHtml(job.description)}</p>

                    <div class="job-skills-section">
                        <span class="skills-label">Required Skills:</span>
                        <div class="skills-list">
                            ${skillsList.map(skill => `
                                <button type="button" class="skill-tag" data-skill="${escapeHtml(skill)}" title="Click to search skill">
                                    ${escapeHtml(skill)}
                                </button>
                            `).join('')}
                        </div>
                    </div>
                </div>

                <div class="job-card-footer">
                    <span class="meta-tag">🎓 ${escapeHtml(job.qualification)}</span>
                    <button class="button button-outline-purple view-details-btn" data-id="${job.id}">
                        View Details <span>→</span>
                    </button>
                </div>
            </article>
        `;
    }).join('');

    // Attach listeners for card actions
    container.querySelectorAll('.view-details-btn').forEach(btn => {
        btn.addEventListener('click', () => openJobDetails(parseInt(btn.dataset.id, 10)));
    });

    container.querySelectorAll('.job-save-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            toggleSaveJob(parseInt(btn.dataset.id, 10));
        });
    });

    container.querySelectorAll('.skill-tag').forEach(tag => {
        tag.addEventListener('click', () => {
            const skill = tag.dataset.skill;
            document.getElementById('job-keyword-input').value = skill;
            fetchAndRenderJobs();
        });
    });
}

function renderFeaturedJobs(featured) {
    const featuredContainer = document.getElementById('featured-jobs-grid');
    if (!featuredContainer) return;

    featuredContainer.innerHTML = featured.map(job => `
        <div class="job-card featured-card">
            <div>
                <div class="job-card-top">
                    <div class="company-logo-pill">${escapeHtml(job.company.charAt(0))}</div>
                    <span class="card-badge">FEATURED</span>
                </div>
                <h3 class="job-role-title">${escapeHtml(job.title)}</h3>
                <span class="job-company-name">${escapeHtml(job.company)} · ${escapeHtml(job.location)}</span>
                <div class="job-tags-row">
                    <span class="meta-tag salary-tag">₹ ${escapeHtml(job.salary)}</span>
                    <span class="meta-tag category-tag">${escapeHtml(job.category)}</span>
                </div>
                <p class="job-description-snippet">${escapeHtml(job.description)}</p>
            </div>
            <div class="job-card-footer">
                <button class="button button-outline-purple view-details-btn" data-id="${job.id}">
                    Explore Role <span>→</span>
                </button>
            </div>
        </div>
    `).join('');

    featuredContainer.querySelectorAll('.view-details-btn').forEach(btn => {
        btn.addEventListener('click', () => openJobDetails(parseInt(btn.dataset.id, 10)));
    });
}

async function loadStats() {
    try {
        const response = await fetch('/api/stats');
        if (response.ok) {
            const stats = await response.json();
            const totalElement = document.getElementById('stat-total-jobs');
            if (totalElement) totalElement.textContent = stats.totalJobs;
        }
    } catch (e) {
        console.warn('Could not load stats:', e);
    }
}

/* ==========================================================================
   JOB DETAILS MODAL
   ========================================================================== */
async function openJobDetails(jobId) {
    const modal = document.getElementById('job-details-modal');
    const content = document.getElementById('modal-job-content');

    content.innerHTML = `<div class="loading-state"><div class="spinner"></div><p>Loading role details...</p></div>`;
    modal.hidden = false;

    try {
        const response = await fetch(`/api/jobs/${jobId}`);
        if (!response.ok) throw new Error('Job not found.');
        const job = await response.json();

        const isSaved = AppState.savedJobs.has(job.id);
        const skillsList = (job.skills || '').split(',').map(s => s.trim()).filter(Boolean);

        content.innerHTML = `
            <div class="modal-job-header">
                <div class="card-tag">JOB ID: #${job.id} · ${escapeHtml(job.category.toUpperCase())}</div>
                <h2 id="modal-job-title">${escapeHtml(job.title)}</h2>
                <span class="modal-company-title">${escapeHtml(job.company)} · ${escapeHtml(job.location)}</span>
            </div>

            <div class="modal-meta-grid">
                <div class="modal-meta-item">
                    <small>Salary Range</small>
                    <strong>₹ ${escapeHtml(job.salary)}</strong>
                </div>
                <div class="modal-meta-item">
                    <small>Experience</small>
                    <strong>${escapeHtml(job.experience)}</strong>
                </div>
                <div class="modal-meta-item">
                    <small>Employment Type</small>
                    <strong>${escapeHtml(job.employmentType || 'Full-time')}</strong>
                </div>
                <div class="modal-meta-item">
                    <small>Required Degree</small>
                    <strong>${escapeHtml(job.qualification)}</strong>
                </div>
            </div>

            <div class="modal-section-block">
                <h4>Job Description</h4>
                <p>${escapeHtml(job.description)}</p>
            </div>

            <div class="modal-section-block">
                <h4>Required Competencies & Skills</h4>
                <div class="skills-list">
                    ${skillsList.map(s => `<span class="chip matched">✓ ${escapeHtml(s)}</span>`).join('')}
                </div>
            </div>

            <div class="modal-apply-banner">
                <strong>Academic Demonstration Note:</strong>
                <div>This is an academic DSA-3 dataset listing. To apply, prospective applicants visit the employer's official career portal at <code>${escapeHtml(job.applyUrl || 'https://careers.' + job.company.toLowerCase().replace(/[^a-z0-9]/g, '') + '.com')}</code>.</div>
            </div>

            <div class="modal-actions-bar">
                <button class="button button-outline-purple" id="modal-save-btn">
                    ${isSaved ? '★ Saved to Bookmarks' : '☆ Save Job'}
                </button>
                <button class="button button-primary" id="modal-match-btn">
                    Match Against My Skills <span>↗</span>
                </button>
                <a href="${escapeHtml(job.applyUrl || '#')}" target="_blank" rel="noopener noreferrer" class="button button-purple" id="modal-apply-link">
                    Apply on Career Portal ↗
                </a>
            </div>
        `;

        document.getElementById('modal-save-btn').addEventListener('click', () => {
            toggleSaveJob(job.id);
            const btn = document.getElementById('modal-save-btn');
            const nowSaved = AppState.savedJobs.has(job.id);
            btn.textContent = nowSaved ? '★ Saved to Bookmarks' : '☆ Save Job';
        });

        document.getElementById('modal-match-btn').addEventListener('click', () => {
            modal.hidden = true;
            document.getElementById('candidate-skills-input').value = job.skills;
            document.getElementById('skill-matcher').scrollIntoView({ behavior: 'smooth' });
            document.getElementById('execute-skill-match-btn').click();
        });

    } catch (error) {
        content.innerHTML = `<div class="empty-results-box"><h3>Error</h3><p>${error.message}</p></div>`;
    }
}

/* ==========================================================================
   SAVED JOBS MANAGEMENT (LOCAL STORAGE)
   ========================================================================== */
function initSavedJobs() {
    updateSavedCounter();

    const savedToggle = document.getElementById('saved-jobs-toggle');
    const savedModal = document.getElementById('saved-jobs-modal');
    const savedClose = document.getElementById('saved-modal-close-btn');
    const clearAllBtn = document.getElementById('clear-all-saved-btn');

    if (savedToggle) {
        savedToggle.addEventListener('click', openSavedJobsModal);
    }
    if (savedClose) {
        savedClose.addEventListener('click', () => { savedModal.hidden = true; });
    }
    if (clearAllBtn) {
        clearAllBtn.addEventListener('click', () => {
            AppState.savedJobs.clear();
            localStorage.setItem('intellijobs_saved_ids', JSON.stringify([]));
            updateSavedCounter();
            renderSavedJobsList();
            fetchAndRenderJobs();
            showToast('Cleared all saved jobs.');
        });
    }
}

function toggleSaveJob(jobId) {
    if (AppState.savedJobs.has(jobId)) {
        AppState.savedJobs.delete(jobId);
        showToast('Job removed from saved bookmarks.');
    } else {
        AppState.savedJobs.add(jobId);
        showToast('Job bookmarked to saved opportunities.');
    }

    localStorage.setItem('intellijobs_saved_ids', JSON.stringify(Array.from(AppState.savedJobs)));
    updateSavedCounter();

    // Update buttons in DOM
    document.querySelectorAll(`.job-save-btn[data-id="${jobId}"]`).forEach(btn => {
        const isSaved = AppState.savedJobs.has(jobId);
        btn.classList.toggle('saved', isSaved);
        btn.textContent = isSaved ? '★' : '☆';
    });
}

function updateSavedCounter() {
    const badge = document.getElementById('saved-count');
    if (badge) badge.textContent = AppState.savedJobs.size;
}

function openSavedJobsModal() {
    const modal = document.getElementById('saved-jobs-modal');
    modal.hidden = false;
    renderSavedJobsList();
}

function renderSavedJobsList() {
    const list = document.getElementById('saved-jobs-list');
    if (AppState.savedJobs.size === 0) {
        list.innerHTML = `
            <div class="empty-results-box" style="padding: 30px;">
                <div class="empty-icon">★</div>
                <h3>No Saved Jobs Yet</h3>
                <p>Click the bookmark icon (☆) on any job card to save roles you wish to review later.</p>
            </div>
        `;
        return;
    }

    const savedIds = Array.from(AppState.savedJobs);
    const savedJobsList = AppState.allJobs.filter(j => savedIds.includes(j.id));

    list.innerHTML = `
        <div class="saved-jobs-list-items">
            ${savedJobsList.map(job => `
                <div class="saved-item-row">
                    <div class="saved-item-info">
                        <strong>${escapeHtml(job.title)}</strong>
                        <small>${escapeHtml(job.company)} · ${escapeHtml(job.location)} · ₹ ${escapeHtml(job.salary)}</small>
                    </div>
                    <div style="display: flex; gap: 8px;">
                        <button class="button button-outline-purple view-details-btn" data-id="${job.id}">View</button>
                        <button class="button button-outline-purple remove-saved-btn" data-id="${job.id}" style="color: #EF4444; border-color: #FECDD3;">Remove</button>
                    </div>
                </div>
            `).join('')}
        </div>
    `;

    list.querySelectorAll('.view-details-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.getElementById('saved-jobs-modal').hidden = true;
            openJobDetails(parseInt(btn.dataset.id, 10));
        });
    });

    list.querySelectorAll('.remove-saved-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            toggleSaveJob(parseInt(btn.dataset.id, 10));
            renderSavedJobsList();
        });
    });
}

/* ==========================================================================
   3. SKILL MATCHER (JACCARD SIMILARITY REST API)
   ========================================================================== */
function initSkillMatcher() {
    const input = document.getElementById('candidate-skills-input');
    const executeBtn = document.getElementById('execute-skill-match-btn');
    const resultsContainer = document.getElementById('match-results-container');
    const resultsCount = document.getElementById('match-results-count');
    const presetButtons = document.querySelectorAll('.preset-btn');

    presetButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            input.value = btn.dataset.preset;
            executeBtn.click();
        });
    });

    executeBtn.addEventListener('click', async () => {
        const skillsText = input.value.trim();
        if (!skillsText) {
            showToast('Please enter at least one skill or select a preset.');
            input.focus();
            return;
        }

        resultsContainer.innerHTML = `
            <div class="loading-state">
                <div class="spinner"></div>
                <p>Computing Jaccard Similarity on Java backend...</p>
            </div>
        `;

        try {
            const response = await fetch('/api/jobs/match', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ skills: skillsText })
            });

            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.message || 'Error executing skill match.');
            }

            const matches = await response.json();
            resultsCount.textContent = `${matches.length} Roles Evaluated`;

            if (matches.length === 0) {
                resultsContainer.innerHTML = `
                    <div class="matcher-placeholder">
                        <span class="placeholder-icon">⌕</span>
                        <h4>No Strong Matches</h4>
                        <p>No roles exceeded threshold similarity. Try adding more general engineering skills.</p>
                    </div>
                `;
                return;
            }

            resultsContainer.innerHTML = matches.map((match, idx) => {
                const scorePercent = match.percentage != null ? match.percentage : Math.round(match.score * 100);
                const scoreClass = scorePercent >= 60 ? 'high' : (scorePercent >= 30 ? 'medium' : 'low');

                return `
                    <div class="matched-item-card">
                        <div class="matched-item-header">
                            <div>
                                <small>#${idx + 1} RANKED RECOMMENDATION</small>
                                <strong>${escapeHtml(match.job.title)}</strong>
                                <small>${escapeHtml(match.job.company)} · ${escapeHtml(match.job.location)} · ₹ ${escapeHtml(match.job.salary)}</small>
                            </div>
                            <span class="score-badge ${scoreClass}">
                                ${scorePercent}% Match
                            </span>
                        </div>

                        <div class="match-progress-bar-wrap">
                            <div class="match-progress-fill" style="width: ${Math.min(100, Math.max(8, scorePercent))}%;"></div>
                        </div>

                        <div class="skills-diff-section">
                            <div class="diff-group">
                                <span class="diff-label">Matched:</span>
                                ${match.matchedSkills.length > 0
                                    ? match.matchedSkills.map(s => `<span class="chip matched">✓ ${escapeHtml(s)}</span>`).join('')
                                    : `<span style="color: var(--text-muted);">None</span>`
                                }
                            </div>
                            <div class="diff-group">
                                <span class="diff-label">Missing:</span>
                                ${match.missingSkills.length > 0
                                    ? match.missingSkills.map(s => `<span class="chip missing">+ ${escapeHtml(s)}</span>`).join('')
                                    : `<span style="color: #059669; font-weight: 600;">Full Skill Coverage!</span>`
                                }
                            </div>
                        </div>

                        <div class="explanation-line">
                            ${escapeHtml(match.explanation || `Jaccard Index: ${match.score.toFixed(2)}`)}
                        </div>

                        <div class="matched-item-actions">
                            <button class="button button-outline-purple view-details-btn" data-id="${match.job.id}">
                                View Role <span>→</span>
                            </button>
                        </div>
                    </div>
                `;
            }).join('');

            resultsContainer.querySelectorAll('.view-details-btn').forEach(btn => {
                btn.addEventListener('click', () => openJobDetails(parseInt(btn.dataset.id, 10)));
            });

        } catch (error) {
            resultsContainer.innerHTML = `<div class="empty-results-box"><h3>Match Failed</h3><p>${error.message}</p></div>`;
            showToast(`Error: ${error.message}`);
        }
    });
}

/* ==========================================================================
   4. ALGORITHM LAB (STRING BENCHMARKS & GRAPH DEMO REST APIS)
   ========================================================================== */
function initAlgorithmLab() {
    const tabStringBtn = document.getElementById('lab-tab-string');
    const tabGraphBtn = document.getElementById('lab-tab-graph');
    const contentString = document.getElementById('tab-string-matching');
    const contentGraph = document.getElementById('tab-graph-algorithms');

    tabStringBtn.addEventListener('click', () => {
        tabStringBtn.classList.add('active');
        tabGraphBtn.classList.remove('active');
        contentString.classList.add('active');
        contentGraph.classList.remove('active');
    });

    tabGraphBtn.addEventListener('click', () => {
        tabGraphBtn.classList.add('active');
        tabStringBtn.classList.remove('active');
        contentGraph.classList.add('active');
        contentString.classList.remove('active');
    });

    // String Matching Execution
    const runSingleBtn = document.getElementById('run-single-algo-btn');
    const runBenchmarkBtn = document.getElementById('run-benchmark-btn');
    const patternInput = document.getElementById('lab-pattern-input');
    const algoSelect = document.getElementById('lab-algo-select');

    runSingleBtn.addEventListener('click', async () => {
        const pattern = patternInput.value.trim();
        const algo = algoSelect.value;
        if (!pattern) {
            showToast('Enter a search pattern or keyword.');
            return;
        }

        const metricsCard = document.getElementById('single-algo-metrics');
        metricsCard.hidden = false;
        document.getElementById('benchmark-results-card').hidden = true;

        try {
            const response = await fetch('/api/algorithms/search', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ pattern: pattern, algorithm: algo })
            });

            if (!response.ok) throw new Error('Algorithm execution failed.');
            const data = await response.json();

            document.getElementById('metric-algo-name').textContent = data.algorithm;
            document.getElementById('metric-exec-time').textContent = `${data.executionTimeNano.toLocaleString()} ns (${data.executionTimeMs} ms)`;
            document.getElementById('metric-records-scanned').textContent = data.recordsProcessed;
            document.getElementById('metric-matches-found').textContent = data.matchesFound;
            document.getElementById('metric-time-comp').textContent = data.timeComplexity;
            document.getElementById('metric-space-comp').textContent = data.spaceComplexity;
            document.getElementById('metric-algo-explanation').textContent = data.explanation;

            const previewContainer = document.getElementById('lab-matched-jobs-list');
            if (data.matchedJobs && data.matchedJobs.length > 0) {
                previewContainer.innerHTML = data.matchedJobs.slice(0, 6).map(j => `
                    <div class="mini-job-item">
                        <strong>${escapeHtml(j.title)}</strong>
                        <span>${escapeHtml(j.company)} · ${escapeHtml(j.location)}</span>
                    </div>
                `).join('');
            } else {
                previewContainer.innerHTML = `<p style="color: var(--text-muted); font-size: 13px;">No job records contained pattern "${escapeHtml(pattern)}".</p>`;
            }

            showToast(`Executed ${data.algorithm} in ${data.executionTimeNano.toLocaleString()} ns.`);
        } catch (e) {
            showToast(`Error: ${e.message}`);
        }
    });

    // Run All & Compare (Benchmark)
    runBenchmarkBtn.addEventListener('click', async () => {
        const pattern = patternInput.value.trim();
        if (!pattern) {
            showToast('Enter a search pattern to compare.');
            return;
        }

        const benchmarkCard = document.getElementById('benchmark-results-card');
        const tbody = document.getElementById('benchmark-table-body');
        document.getElementById('single-algo-metrics').hidden = true;
        benchmarkCard.hidden = false;

        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 20px;"><div class="spinner"></div>Measuring algorithms in JVM...</td></tr>`;

        try {
            const response = await fetch('/api/algorithms/compare', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ pattern: pattern })
            });

            if (!response.ok) throw new Error('Benchmark failed.');
            const data = await response.json();

            document.getElementById('benchmark-subtitle').textContent = `Pattern: "${escapeHtml(data.pattern)}" evaluated across ${data.recordsProcessed} job listings`;
            document.getElementById('benchmark-note').textContent = data.note;

            tbody.innerHTML = data.benchmarks.map(b => `
                <tr>
                    <td><strong>${escapeHtml(b.name)}</strong></td>
                    <td><span class="chip matched">${b.matchesFound} roles</span></td>
                    <td><strong>${b.timeMicro} µs</strong></td>
                    <td>${b.timeNano.toLocaleString()} ns</td>
                    <td><code>${escapeHtml(b.timeComplexity)}</code></td>
                    <td><code>${escapeHtml(b.spaceComplexity)}</code></td>
                    <td style="color: var(--text-muted); font-size: 12.5px;">${escapeHtml(b.notes)}</td>
                </tr>
            `).join('');

            showToast('Benchmark completed with actual JVM System.nanoTime() metrics.');
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="7" style="color: red; text-align: center;">Error: ${e.message}</td></tr>`;
        }
    });

    // Graph Algorithm Selector
    const graphButtons = document.querySelectorAll('.graph-selector-btn');
    graphButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            graphButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const graphType = btn.dataset.graph;
            AppState.activeGraphDemo = graphType;
            loadGraphDemo(graphType);
        });
    });
}

async function loadGraphDemo(graphType, source, target) {
    const title = document.getElementById('graph-demo-title');
    const scenario = document.getElementById('graph-demo-scenario');
    const controls = document.getElementById('graph-controls-inline');
    const nodesVisual = document.getElementById('graph-nodes-visual');
    const ribbon = document.getElementById('traversal-ribbon');
    const logStream = document.getElementById('graph-log-stream');
    const timeComp = document.getElementById('graph-time-comp');
    const spaceComp = document.getElementById('graph-space-comp');
    const domainText = document.getElementById('graph-domain-text');

    logStream.innerHTML = `<div class="log-entry info">Executing ${graphType.toUpperCase()} on backend...</div>`;

    const params = new URLSearchParams({ type: graphType });
    if (source != null) params.append('source', source);
    if (target != null) params.append('target', target);

    try {
        const response = await fetch(`/api/graph/demo?${params.toString()}`);
        if (!response.ok) throw new Error('Graph demo request failed.');
        const data = await response.json();

        title.textContent = data.algorithm;
        scenario.textContent = `Scenario: ${data.scenario}`;
        timeComp.textContent = data.timeComplexity;
        spaceComp.textContent = data.spaceComplexity;
        domainText.textContent = data.explanation;

        // Dynamic controls based on algorithm
        if (graphType === 'bfs' || graphType === 'dfs') {
            controls.innerHTML = `
                <label style="font-size: 12px; font-weight: 700;">Start Node:</label>
                <select class="graph-select-control" id="graph-start-select">
                    ${Object.entries(data.vertexLabels).map(([id, label]) => `
                        <option value="${id}" ${parseInt(id, 10) === data.startVertex ? 'selected' : ''}>${id}: ${escapeHtml(label)}</option>
                    `).join('')}
                </select>
            `;
            document.getElementById('graph-start-select').addEventListener('change', (e) => {
                loadGraphDemo(graphType, parseInt(e.target.value, 10));
            });

            // Nodes visual
            nodesVisual.innerHTML = Object.entries(data.vertexLabels).map(([id, label]) => {
                const numId = parseInt(id, 10);
                const orderIdx = data.traversalOrder.indexOf(numId);
                const level = data.levels ? data.levels[numId] : null;

                return `
                    <div class="graph-node-pill active">
                        <span class="node-id-badge">${id}</span>
                        <span class="node-name">${escapeHtml(label)}</span>
                        ${level != null ? `<span class="node-level">Level ${level}</span>` : ''}
                        <span class="chip matched">Step #${orderIdx + 1}</span>
                    </div>
                `;
            }).join('');

            // Ribbon
            ribbon.innerHTML = `
                <strong>${graphType.toUpperCase()} Traversal Sequence:</strong>
                <div class="ribbon-tags">
                    ${data.traversalLabels.map((lbl, i) => `
                        <span class="ribbon-tag">${escapeHtml(lbl)}</span>
                        ${i < data.traversalLabels.length - 1 ? `<span class="ribbon-arrow">→</span>` : ''}
                    `).join('')}
                </div>
            `;

            // Logs
            if (data.steps) {
                logStream.innerHTML = data.steps.map(step => {
                    if (graphType === 'bfs') {
                        return `<div class="log-entry">Visited Node [${step.current}]: <strong>${escapeHtml(step.label)}</strong> (Level ${step.level}) | Queue: [${step.queueState.join(', ')}]</div>`;
                    } else {
                        return `<div class="log-entry ${step.action === 'VISIT' ? 'success' : 'info'}">${step.action} Node [${step.vertex}]: ${escapeHtml(step.label)} (Depth ${step.depth})</div>`;
                    }
                }).join('');
            }

        } else if (graphType === 'dijkstra') {
            controls.innerHTML = `
                <label style="font-size: 12px; font-weight: 700;">From:</label>
                <select class="graph-select-control" id="dijkstra-source-select">
                    ${Object.entries(data.vertexLabels).map(([id, label]) => `
                        <option value="${id}" ${parseInt(id, 10) === data.sourceVertex ? 'selected' : ''}>${id}: ${escapeHtml(label)}</option>
                    `).join('')}
                </select>
                <label style="font-size: 12px; font-weight: 700;">To:</label>
                <select class="graph-select-control" id="dijkstra-target-select">
                    ${Object.entries(data.vertexLabels).map(([id, label]) => `
                        <option value="${id}" ${parseInt(id, 10) === data.targetVertex ? 'selected' : ''}>${id}: ${escapeHtml(label)}</option>
                    `).join('')}
                </select>
                <button class="button button-primary" id="dijkstra-recalc-btn" style="padding: 6px 14px; font-size: 12px;">Compute Shortest Path</button>
            `;

            document.getElementById('dijkstra-recalc-btn').addEventListener('click', () => {
                const s = parseInt(document.getElementById('dijkstra-source-select').value, 10);
                const t = parseInt(document.getElementById('dijkstra-target-select').value, 10);
                loadGraphDemo('dijkstra', s, t);
            });

            // Nodes visual for Dijkstra
            nodesVisual.innerHTML = Object.entries(data.vertexLabels).map(([id, label]) => {
                const numId = parseInt(id, 10);
                const inPath = data.shortestPath.includes(numId);
                const dist = data.allDistances[numId];

                return `
                    <div class="graph-node-pill ${inPath ? 'active' : ''}">
                        <span class="node-id-badge">${id}</span>
                        <span class="node-name">${escapeHtml(label)}</span>
                        <span class="node-level">${dist >= 0 ? `${dist} weeks effort` : 'Unreachable'}</span>
                        ${inPath ? `<span class="chip matched">★ ON SHORTEST PATH</span>` : ''}
                    </div>
                `;
            }).join('');

            // Ribbon
            ribbon.innerHTML = `
                <strong>Optimal Learning Pathway (Total Effort: ${data.totalEffortWeeks} weeks):</strong>
                <div class="ribbon-tags">
                    ${data.pathLabels.map((lbl, i) => `
                        <span class="ribbon-tag">${escapeHtml(lbl)}</span>
                        ${i < data.pathLabels.length - 1 ? `<span class="ribbon-arrow">→</span>` : ''}
                    `).join('')}
                </div>
            `;

            // Logs
            logStream.innerHTML = `
                <div class="log-entry success">Dijkstra Initialized: Source = ${data.sourceLabel}, Target = ${data.targetLabel}</div>
                ${data.relaxationLog.map(log => `<div class="log-entry">${escapeHtml(log)}</div>`).join('')}
                <div class="log-entry success">Optimal Shortest Distance = ${data.totalEffortWeeks} weeks effort.</div>
            `;

        } else if (graphType === 'bipartite') {
            controls.innerHTML = `<span class="card-badge" style="font-size: 13px;">${data.maxMatches} / ${data.totalCandidates} Matches Achieved</span>`;

            // Visual: assignments
            nodesVisual.innerHTML = data.assignments.map(pair => `
                <div class="graph-node-pill active" style="flex-direction: column; align-items: flex-start; gap: 6px;">
                    <div style="display: flex; justify-content: space-between; width: 100%; align-items: center;">
                        <strong style="color: var(--primary); font-size: 14px;">${escapeHtml(pair.candidateName)}</strong>
                        <span class="chip matched">✓ Matched with Role #${pair.jobId}</span>
                    </div>
                    <div style="font-size: 12.5px; color: var(--text-muted);">
                        Assigned Role: <strong>${escapeHtml(pair.jobTitle)}</strong> (${escapeHtml(pair.company)})
                    </div>
                    <div style="font-size: 11.5px; color: var(--text-light);">
                        Candidate Skills: ${pair.candidateSkills.join(', ')}
                    </div>
                </div>
            `).join('');

            ribbon.innerHTML = `
                <strong>Optimal Maximum Matching Allocation:</strong>
                <div style="font-size: 13px; margin-top: 6px;">
                    Assigned <strong>${data.maxMatches}</strong> candidates to non-overlapping job roles with maximum cardinality using Augmenting Paths.
                </div>
            `;

            logStream.innerHTML = `
                <div class="log-entry success">Maximum Bipartite Matching using Augmenting Paths:</div>
                ${data.augmentingPathLog.map(log => `<div class="log-entry">${escapeHtml(log)}</div>`).join('')}
                <div class="log-entry success">Total Cardinality: ${data.maxMatches} candidates assigned.</div>
            `;
        }

    } catch (e) {
        logStream.innerHTML = `<div class="log-entry" style="color: red;">Error: ${e.message}</div>`;
    }
}

/* ==========================================================================
   MODAL CONTROLS & ESCAPE HANDLER
   ========================================================================== */
function initModals() {
    const jobModal = document.getElementById('job-details-modal');
    const jobModalClose = document.getElementById('modal-close-btn');
    const savedModal = document.getElementById('saved-jobs-modal');
    const savedModalClose = document.getElementById('saved-modal-close-btn');

    if (jobModalClose && jobModal) {
        jobModalClose.addEventListener('click', () => { jobModal.hidden = true; });
        jobModal.addEventListener('click', (e) => {
            if (e.target === jobModal) jobModal.hidden = true;
        });
    }

    if (savedModalClose && savedModal) {
        savedModalClose.addEventListener('click', () => { savedModal.hidden = true; });
        savedModal.addEventListener('click', (e) => {
            if (e.target === savedModal) savedModal.hidden = true;
        });
    }

    window.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            if (jobModal) jobModal.hidden = true;
            if (savedModal) savedModal.hidden = true;
        }
    });
}

/* ==========================================================================
   TOAST NOTIFICATION HELPER
   ========================================================================== */
function showToast(message) {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `<span>ℹ</span> <span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3200);
}

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}