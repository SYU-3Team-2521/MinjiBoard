// ===== API & Utils =====
const API = {
    list:   ({category, page=0, size=9, sort="likeCount,desc"}) =>
        `/api/posts?${toParams({category, page, size, sort})}`,
    detail: (id)   => `/api/posts/${id}`,
    create: ()     => `/api/posts`, // multipart only
    like:   (id)   => `/api/posts/${id}/like`,
    comments: {
        list:  (postId, page=0, size=10, sort="createdAt,desc") =>
            `/api/posts/${postId}/comments?${toParams({page,size,sort})}`,
        create:(postId) => `/api/posts/${postId}/comments`,
    },
};

const CATEGORY_LABELS = {
    "": "전체",
    "MARKET": "전통시장&마트",
    "PHARMACY_HOSPITAL": "약국&병원",
    "FOOD_CAFE_CONVENIENCE": "음식점&카페&편의점",
    "ETC": "기타"
};
const CAT_KEYS = Object.keys(CATEGORY_LABELS).filter(k=>k!=="");
function catLabel(code){ return CATEGORY_LABELS[code] || code; }

function toParams(obj){
    const sp = new URLSearchParams();
    Object.entries(obj).forEach(([k,v])=>{
        if (v===undefined || v===null || v==="") return;
        sp.set(k, v);
    });
    return sp.toString();
}
async function jget(url){
    const r = await fetch(url, {credentials:"include"});
    if(!r.ok) throw await toErr(r);
    return r.json();
}
async function jpost(url, body){
    const r = await fetch(url, {
        method:"POST",
        headers:{ "Content-Type":"application/json" },
        credentials:"include",
        body: JSON.stringify(body||{})
    });
    if(!r.ok) throw await toErr(r);
    return r.json();
}
async function toErr(res){
    try {
        const j = await res.json();
        if (j && j.message) return new Error(j.message);
    } catch(_) {}
    return new Error(`${res.status} ${res.statusText}`);
}
function esc(s){ return String(s??"").replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }
function v(sel){ return document.querySelector(sel).value.trim(); }

// ===== Router =====
window.addEventListener("hashchange", route);
function route(){
    document.title = "민생-췍";
    const app = document.getElementById("app");
    const hash = location.hash || "#/";

    if (hash.startsWith("#/new"))    return renderNew(app);
    if (hash.startsWith("#/post/"))  return renderDetail(app, Number(hash.split("/")[2]));
    return renderList(app);
}

// ===== List View =====
async function renderList(root){
    root.innerHTML = `
    <section class="panel">
      <div class="toolbar">
        <div class="pills" id="pill-wrap">
          ${['', ...CAT_KEYS].map(c=>{
        const cls = c==='' ? "pill active" : "pill";
        return `<button class="${cls}" data-cat="${c}">${esc(CATEGORY_LABELS[c])}</button>`;
    }).join('')}
        </div>
        <div class="sorttabs">
          <a href="javascript:void(0)" data-sort="createdAt,desc" id="sort-new">최신순</a>
          <a href="javascript:void(0)" data-sort="likeCount,desc" id="sort-pop" class="active">인기순</a>
        </div>
      </div>

      <div id="grid" class="grid"></div>
      <div id="pager" class="pager"></div>
    </section>
  `;

    const state = { category:"", sort:"likeCount,desc", page:0, size:9, q:"" };

    // 헤더 검색
    const searchInput = document.getElementById("search-input");
    const searchBtn   = document.getElementById("search-btn");
    const applySearch = ()=>{
        state.q = (searchInput?.value||"").trim();
        state.page = 0; load();
    };
    searchBtn?.addEventListener("click", applySearch);
    searchInput?.addEventListener("keydown", (e)=>{ if(e.key==="Enter") applySearch(); });

    // 카테고리
    document.getElementById("pill-wrap").addEventListener("click", (e)=>{
        const b = e.target.closest("button[data-cat]");
        if(!b) return;
        state.category = b.dataset.cat;
        state.page = 0;
        document.querySelectorAll("#pill-wrap .pill").forEach(x=>x.classList.remove("active"));
        b.classList.add("active");
        load();
    });

    // 정렬
    const sortNew = document.getElementById("sort-new");
    const sortPop = document.getElementById("sort-pop");
    function setSort(s){
        state.sort = s; state.page = 0; load();
        sortNew.classList.toggle("active", s==="createdAt,desc");
        sortPop.classList.toggle("active", s==="likeCount,desc");
    }
    sortNew.addEventListener("click", ()=>setSort("createdAt,desc"));
    sortPop.addEventListener("click", ()=>setSort("likeCount,desc"));

    const grid = document.getElementById("grid");

    async function load(){
        try{
            const data = await jget(API.list({
                category: state.category || undefined,
                page: state.page, size: state.size, sort: state.sort
            }));
            let items = data.content;
            if (state.q) {
                const q = state.q.toLowerCase();
                items = items.filter(p =>
                    (p.name||"").toLowerCase().includes(q) || (p.address||"").toLowerCase().includes(q)
                );
            }
            grid.innerHTML = items.map(card).join("") || `<div class="center muted" style="padding:24px">조건에 맞는 가맹점이 없습니다.</div>`;
            renderPager(data.totalPages, data.page);

            // 하트
            grid.querySelectorAll("button[data-like]").forEach(btn=>{
                btn.addEventListener("click", async ()=>{
                    const id = Number(btn.dataset.id);
                    if(!id) return;
                    btn.disabled = true;
                    try{
                        const r = await jpost(API.like(id), {});
                        btn.querySelector(".cnt").textContent = r.likeCount;
                        btn.classList.add("liked");
                    }catch(e){
                        alert(e.message);
                        btn.disabled = false;
                    }
                });
            });
        }catch(e){
            grid.innerHTML = `<div class="center muted" style="padding:24px">목록을 불러오지 못했습니다.<br/>${esc(e.message)}</div>`;
        }
    }

    function initials(name){
        const s = (name||"").trim();
        if (!s) return "MIN";
        return s.length<=2 ? s : s.slice(0,2);
    }

    function card(p){
        const thumb = p.imgUrl
            ? `<div class="thumb"><img src="${esc(p.imgUrl)}" alt=""></div>`
            : `<div class="thumb">${esc(initials(p.name))}</div>`;
        return `
      <div class="card">
        <div class="thumb-wrap" style="position:relative">
          ${thumb}
          <div class="avatar">🐶</div>
          <button class="likebtn" data-like data-id="${p.id}" title="좋아요">
            <span class="heart">❤️</span><span class="cnt">${p.likeCount}</span>
          </button>
        </div>
        <div class="body">
          <div class="name">${esc(p.name)}</div>
          <div class="meta">
            <span class="muted">${esc(p.address||"")}</span>
            <a href="#/post/${p.id}" class="btn">상세보기</a>
          </div>
          <div class="meta" style="margin-top:6px">
            <span class="cat">${esc(catLabel(p.category))}</span>
            <span class="muted">${esc(p.createdAtFormatted||"")}</span>
          </div>
        </div>
      </div>
    `;
    }

    function renderPager(totalPages, page){
        const wrap = document.getElementById("pager");
        const tp = Math.max(1, totalPages);
        const cur = page + 1;
        let start = Math.max(1, cur-2);
        let end   = Math.min(tp, start+4);
        start = Math.max(1, end-4);

        let html = '';
        html += `<span class="page ${cur===1?'muted':''}" data-gop="first">«</span>`;
        html += `<span class="page ${cur===1?'muted':''}" data-gop="prev">‹</span>`;
        for (let i=start;i<=end;i++){
            html += `<span class="page ${i===cur?'active':''}" data-page="${i-1}">${i}</span>`;
        }
        html += `<span class="page ${cur===tp?'muted':''}" data-gop="next">›</span>`;
        html += `<span class="page ${cur===tp?'muted':''}" data-gop="last">»</span>`;
        wrap.innerHTML = html;

        wrap.querySelectorAll(".page").forEach(el=>{
            el.addEventListener("click", ()=>{
                if (el.dataset.page){ state.page = Number(el.dataset.page); load(); return; }
                const g = el.dataset.gop;
                if (g==="first") state.page = 0;
                if (g==="prev")  state.page = Math.max(0, state.page-1);
                if (g==="next")  state.page = state.page+1;
                if (g==="last")  state.page = tp-1;
                load();
            });
        });
    }

    load();
}

// ===== Detail View (댓글 포함) =====
async function renderDetail(root, id){
    root.innerHTML = `<section class="panel"><div id="detail" style="padding:18px">불러오는 중…</div></section>`;
    try{
        const d = await jget(API.detail(id));
        const img = d.imgUrl ? `<img src="${esc(d.imgUrl)}" alt="" style="max-width:100%;border-radius:12px;border:1px solid var(--line)"/>` : '';
        document.getElementById("detail").innerHTML = `
      <h2 style="margin:0 0 8px">${esc(d.name)}</h2>
      <div class="muted" style="margin-bottom:6px">${esc(catLabel(d.category))} · ${esc(d.createdAtFormatted||"")}</div>
      <div class="muted" style="margin-bottom:14px">${esc(d.address||"")}</div>
      ${img}
      <div style="margin:14px 0">
        <button id="btn-like" class="btn solid">${d.liked?"이미 좋아요":"좋아요"}</button>
        <span id="like-count" class="cat">like: ${d.likeCount}</span>
        <a class="btn" href="#/">← 목록</a>
      </div>
      <p style="white-space:pre-wrap;line-height:1.6">${esc(d.content||"")}</p>

      <div style="height:1px;background:var(--line);margin:18px 0"></div>

      <section id="comments">
        <h3 style="margin:0 0 10px">댓글</h3>
        <form id="form-comment" style="display:flex;gap:8px;align-items:flex-start;margin:6px 0 12px">
          <textarea id="comment-content" class="search-input" placeholder="댓글을 입력하세요" required style="flex:1;min-height:80px"></textarea>
          <button class="btn solid" type="submit" style="height:40px">등록</button>
        </form>
        <div id="comment-list"></div>
        <div class="pager" id="c-pager"></div>
      </section>
    `;

        // 좋아요
        const btn = document.getElementById("btn-like");
        const badge = document.getElementById("like-count");
        if (d.liked) btn.disabled = true;
        btn.addEventListener("click", async ()=>{
            try{
                const r = await jpost(API.like(id), {});
                badge.textContent = `like: ${r.likeCount}`;
                btn.textContent   = "이미 좋아요";
                btn.disabled = true;
            }catch(e){ alert(e.message); }
        });

        // 댓글
        const cState = { page:0, size:10 };
        document.getElementById("form-comment").addEventListener("submit", async (ev)=>{
            ev.preventDefault();
            const content = document.getElementById("comment-content").value.trim();
            if (!content) return;
            try{
                await jpost(API.comments.create(id), {content});
                document.getElementById("comment-content").value = "";
                cState.page = 0;
                await loadComments();
            }catch(e){ alert(e.message); }
        });

        async function loadComments(){
            try{
                const data = await jget(API.comments.list(id, cState.page, cState.size));
                const box = document.getElementById("comment-list");
                box.innerHTML = data.content.map(c => `
          <div style="display:flex;justify-content:space-between;gap:12px;padding:8px 0;border-bottom:1px dashed var(--line)">
            <div style="white-space:pre-wrap">${esc(c.content)}</div>
            <div class="muted" style="white-space:nowrap">${esc(c.createdAtFormatted||"")}</div>
          </div>
        `).join("") || `<div class="center muted" style="padding:10px">첫 댓글을 남겨보세요.</div>`;

                const tp = Math.max(1, data.totalPages);
                const cur = data.page + 1;
                let start = Math.max(1, cur-2);
                let end   = Math.min(tp, start+4);
                start = Math.max(1, end-4);
                const wrap = document.getElementById("c-pager");
                let html = '';
                html += `<span class="page ${cur===1?'muted':''}" data-gop="first">«</span>`;
                html += `<span class="page ${cur===1?'muted':''}" data-gop="prev">‹</span>`;
                for (let i=start;i<=end;i++){
                    html += `<span class="page ${i===cur?'active':''}" data-page="${i-1}">${i}</span>`;
                }
                html += `<span class="page ${cur===tp?'muted':''}" data-gop="next">›</span>`;
                html += `<span class="page ${cur===tp?'muted':''}" data-gop="last">»</span>`;
                wrap.innerHTML = html;

                wrap.querySelectorAll(".page").forEach(el=>{
                    el.addEventListener("click", ()=>{
                        if (el.dataset.page){ cState.page = Number(el.dataset.page); loadComments(); return; }
                        const g = el.dataset.gop;
                        if (g==="first") cState.page = 0;
                        if (g==="prev")  cState.page = Math.max(0, cState.page-1);
                        if (g==="next")  cState.page = cState.page+1;
                        if (g==="last")  cState.page = tp-1;
                        loadComments();
                    });
                });
            }catch(e){
                document.getElementById("comment-list").innerHTML =
                    `<div class="center muted" style="padding:10px">댓글을 불러오지 못했습니다.<br/>${esc(e.message)}</div>`;
            }
        }
        loadComments();

    }catch(e){
        document.getElementById("detail").innerHTML = `<div class="muted">상세 불러오기 실패<br/>${esc(e.message)}</div>`;
    }
}

// ===== New View (파일 업로드만 지원, multipart) =====
function renderNew(root){
    root.innerHTML = `
    <section class="panel" style="padding:18px">
      <h2 style="margin:0 0 10px">게시글 작성</h2>
      <form id="form-new" class="row" enctype="multipart/form-data"
            style="display:grid;grid-template-columns:1fr 1fr;gap:12px">
        <label>카테고리
          <select id="n-category" class="search-input" required>
            <option value="" disabled selected>선택하세요</option>
            ${CAT_KEYS.map(c=>`<option value="${c}">${esc(CATEGORY_LABELS[c])}</option>`).join("")}
          </select>
        </label>
        <label>가게 이름<input id="n-name" class="search-input" required maxlength="20"/></label>
        <label style="grid-column:1/3">주소<input id="n-address" class="search-input" required maxlength="255"/></label>
        <label style="grid-column:1/3">설명<textarea id="n-content" class="search-input" style="min-height:120px" required maxlength="2000"></textarea></label>
        <label style="grid-column:1/3">대표 이미지 파일(선택)
          <input id="n-file" type="file" class="search-input" accept="image/*"/>
        </label>
        <div style="grid-column:1/3;display:flex;gap:8px;justify-content:flex-end">
          <a class="btn" href="#/">취소</a>
          <button class="btn solid" type="submit">등록</button>
        </div>
      </form>
    </section>
  `;
    document.getElementById("form-new").addEventListener("submit", async (ev)=>{
        ev.preventDefault();
        const fd = new FormData();
        fd.append("category", v("#n-category"));
        fd.append("name",     v("#n-name"));
        fd.append("address",  v("#n-address"));
        fd.append("content",  v("#n-content"));
        const f = document.querySelector("#n-file").files[0];
        if (f) fd.append("image", f);

        try{
            const res = await fetch(API.create(), { method:"POST", body: fd, credentials:"include" });
            if (!res.ok) throw await toErr(res);
            const r = await res.json();
            location.hash = `#/post/${r.id}`;
        }catch(e){
            alert(e.message || "등록 실패");
        }
    });
}

// --- 안전 부트스트랩: DOM 상태와 무관하게 한 번은 route() 실행 ---
(function bootstrap(){
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', route, { once: true });
    } else {
        route();
    }
})();
