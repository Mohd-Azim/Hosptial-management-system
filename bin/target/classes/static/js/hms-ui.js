/**
 * Debounced table filtering + patient prefix search (Redis → API → DB on server).
 */
(function (global) {
    function debounce(fn, ms) {
        var t;
        return function () {
            var ctx = this;
            var args = arguments;
            clearTimeout(t);
            t = setTimeout(function () {
                fn.apply(ctx, args);
            }, ms);
        };
    }

    function bindTableFilter(inputSelector, tableSelector, rowAttr) {
        var input = document.querySelector(inputSelector);
        var table = document.querySelector(tableSelector);
        if (!input || !table) {
            return;
        }
        var tbody = table.querySelector("tbody");
        if (!tbody) {
            return;
        }
        var rows = tbody.querySelectorAll("tr");
        var run = debounce(function () {
            var q = (input.value || "").trim().toLowerCase();
            for (var i = 0; i < rows.length; i++) {
                var tr = rows[i];
                var blob = (tr.getAttribute(rowAttr) || "").toLowerCase();
                tr.style.display = !q || blob.indexOf(q) !== -1 ? "" : "none";
            }
        }, 320);
        input.addEventListener("input", run);
        input.addEventListener("change", run);
    }

    function escapeHtml(s) {
        return String(s)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    function bindPatientSearch(inputSelector, resultsSelector, endpoint) {
        var input = document.querySelector(inputSelector);
        var box = document.querySelector(resultsSelector);
        if (!input || !box) {
            return;
        }
        var run = debounce(function () {
            var q = input.value.trim();
            if (q.length < 2) {
                box.innerHTML = "";
                return;
            }
            fetch(endpoint + "?q=" + encodeURIComponent(q), { credentials: "same-origin" })
                .then(function (r) {
                    return r.ok ? r.json() : Promise.reject(new Error("search failed"));
                })
                .then(function (hits) {
                    if (!hits.length) {
                        box.innerHTML = '<div class="hms-muted">No matches</div>';
                        return;
                    }
                    box.innerHTML = hits
                        .map(function (h) {
                            return (
                                '<div class="hms-hit">' +
                                escapeHtml(h.fullName) +
                                " · MRN " +
                                escapeHtml(h.mrn) +
                                " · #" +
                                h.userId +
                                "</div>"
                            );
                        })
                        .join("");
                })
                .catch(function () {
                    box.textContent = "Search failed";
                });
        }, 350);
        input.addEventListener("input", run);
    }

    global.HMS = global.HMS || {};
    global.HMS.debounce = debounce;
    global.HMS.bindTableFilter = bindTableFilter;
    global.HMS.bindPatientSearch = bindPatientSearch;
})(window);
