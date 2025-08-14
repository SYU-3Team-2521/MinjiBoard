// ====== Config ======
const API = {
    list:   ({category, page=0, size=10, sort="likeCount,desc"}) =>
        `/api/posts?${params({category, page, size, sort})}`,
    detail: (id)   => `/api/posts/${id}`,
    create: ()     => `/api/posts`,
    like:   (id)   => `/api/posts/${id}/like`,
    comments: {
        list:  (postId, page=0, size=10) => `/api/posts/${postId}/comments?${params({page,size,sort:"createdAt,desc"})}`,
        create:(postId) => `/api/posts/${postId}/comments`
    }
};

// 카테고리 한글 라벨
const CATEGORY_LABELS = {
    "MARKET": "전통시장&마트",
    "PHARMACY_HOSPITAL": "약국&병원",
    "FOOD_CAFE_CONVENIENCE": "음식점&카페&편의점",
    "ETC": "기타"
};
function catLabel(code){ return CATEGORY_LABELS[code] || code; }

function params(obj){
    const sp = new URLSearchParams();
    Object.entries(obj).forEach(([k,v])=>{
        if (v===undefined || v===null || v==="") return;
        sp.set(k, v);
    });
    return sp.toString();
}
async function jget(url){
    const res = await fetch(url, {credentials:"include"});
    if(!res.ok) throw await toErr(res);
    return res.json();
}
async function jpost(url, body){
    const res = await fetch(url, {
        method:"POST",
        headers:{ "Content-Type":"application/json" },
        credentials:"include",
        body: JSON.stringify(body||{})
    });
    if(!res.ok) throw await toErr(res);
    return res.json();
}
async function toErr(res){
    let msg = `${res.status} ${res.statusText}`;
    try { const j = await res.json(); msg = `${msg}\n${j.code||""} ${j.message||""}` } catch(_){}
    return new Error(msg);
}

// ====== Simple Router ======
window.addEventListener("hashchange", route);
window.addEventListener("DOMContentLoaded", route);

function route(){
    document.title = "민생-췍"; // 페이지 제목
    const app = document.getElementById("app");
    const hash = location.hash || "#/";
    const m = hash.match(/^#\/post\/(\d+)/);
    document.querySelectorAll('nav a').forEach(a=>a.classList.remove('active'));

    if (hash.startsWith("#/new")) {
        document.getElementById('nav-new').classList.add('active');
        renderNew(app);
    } else if (m) {
        document.getElementById('nav-list').classList.add('active');
        renderDetail(app, Number(m[1]));
    } else {
        document.getElementById('nav-list').classList.add('active');
        renderList(app);
    }
}

// ====== Views ======
async function renderList(root){
    root.innerHTML = `
    <div class="panel">
      <div class="toolbar">
        <label>
          <div class="help">카테고리</div>
          <select id="f-category" class="select">
            <option value="">전체</option>
            ${Object.keys(CATEGORY_LABELS).map(c=>`<option value="${c}">${catLabel(c)}</option>`).join("")}
          </select>
        </label>
        <label>
          <div class="help">페이지 크기</div>
          <select id="f-size" class="select">
            ${[5,10,20].map(n=>`<option ${n===10?"selected":""} value="${n}">${n}</option>`).join("")}
          </select>
        </label>
        <span class="badge">정렬: 좋아요 내림차순</span>
        <span class="spacer"></span>
        <a class="btn primary" href="#/new">+ 글쓰기</a>
      </div>

      <div id="list" class="grid"></div>
      <div class="pager">
        <button class="btn" id="btn-prev">이전</button>
        <div id="page-indicator" class="help"></div>
        <button class="btn" id="btn-next">다음</button>
      </div>
    </div>
  `;

    const state = { category:"", page:0, size:10 };
    const ddlCategory = root.querySelector("#f-category");
    const ddlSize = root.querySelector("#f-size");
    const listEl = root.querySelector("#list");

    ddlCategory.addEventListener('change', ()=>{ state.category = ddlCategory.value||""; state.page=0; load(); });
    ddlSize.addEventListener('change', ()=>{ state.size = Number(ddlSize.value); state.page=0; load(); });
    root.querySelector("#btn-prev").addEventListener('click', ()=>{ state.page=Math.max(0, state.page-1); load(); });
    root.querySelector("#btn-next").addEventListener('click', ()=>{ state.page=state.page+1; load(); });

    // 목록에서 하트(좋아요) 가능 — 이벤트 델리게이션
    listEl.addEventListener('click', async (e)=>{
        const btn = e.target.closest('button[data-like]');
        if (!btn) return;
        const id = Number(btn.dataset.id);
        if (!id) return;

        btn.disabled = true;
        try {
            const r = await jpost(API.like(id), {});
            // 숫자 업데이트 & 버튼 상태 표시
            const cnt = btn.querySelector('.cnt');
            if (cnt) cnt.textContent = r.likeCount;
            btn.classList.add('liked');
            // (서버가 쿠키로 중복 방지하므로, 새로고침 전까지는 버튼 비활성 유지)
        } catch(err) {
            alert("좋아요 실패:\n" + err.message);
            btn.disabled = false;
        }
    });

    async function load(){
        const {category,page,size} = state;
        try{
            const data = await jget(API.list({category, page, size}));
            listEl.innerHTML = data.content.map(card).join("") || `<div class="center help">게시글이 없습니다.</div>`;
            root.querySelector("#page-indicator").textContent = `page ${data.page+1} / ${Math.max(1,data.totalPages)} (총 ${data.totalElements})`;
            root.querySelector("#btn-prev").disabled = data.page<=0;
            root.querySelector("#btn-next").disabled = data.page+1 >= Math.max(1,data.totalPages);
        }catch(e){
            alert("목록 불러오기 실패:\n" + e.message);
        }
    }
    function card(p){
        const img = p.imgUrl ? `<img src="${esc(p.imgUrl)}" alt="">` : `<div class="thumb">이미지 없음</div>`;
        return `
      <div class="card">
        ${img}
        <div class="body">
          <div class="kv"><b>${esc(p.name)}</b></div>
          <div class="kv">분류: ${esc(catLabel(p.category))}</div>
          <div class="kv help">${esc(p.address||"")}</div>
          <div class="kv help">${esc(p.createdAtFormatted||"")}</div>
          <div style="display:flex; align-items:center; gap:8px;">
            <button class="btn heart" data-like data-id="${p.id}" title="좋아요">
              <i class="icon">❤️</i><span class="cnt">${p.likeCount}</span>
            </button>
            <a class="btn" href="#/post/${p.id}">상세보기</a>
          </div>
        </div>
      </div>`;
    }
    load();
}

async function renderDetail(root, id){
    root.innerHTML = `<div class="panel"><div id="detail">불러오는 중…</div></div>`;
    try {
        const data = await jget(API.detail(id));
        const likedClass = data.liked ? "liked" : "";
        const img = data.imgUrl ? `<img src="${esc(data.imgUrl)}" alt="" style="max-height:280px;object-fit:cover;border-radius:8px;border:1px solid #1e2732" />` : "";
        root.querySelector("#detail").innerHTML = `
      <div class="row" style="align-items:flex-start">
        <div style="flex:2; min-width:280px">
          <h2 style="margin:0 0 6px 0">${esc(data.name)}</h2>
          <div class="kv">카테고리: <b>${esc(catLabel(data.category))}</b></div>
          <div class="kv">주소: <b>${esc(data.address||"")}</b></div>
          <div class="kv help">${esc(data.createdAtFormatted||"")}</div>
          <div class="hr"></div>
          <p style="white-space:pre-wrap; line-height:1.5">${esc(data.content||"")}</p>
          <div class="hr"></div>
          <div class="like ${likedClass}">
            <i class="icon">❤️</i>
            <button id="btn-like" class="btn primary">${data.liked?"이미 좋아요":"좋아요"}</button>
            <span id="like-count" class="badge">like: ${data.likeCount}</span>
            <a class="btn" style="margin-left:auto" href="#/">← 목록</a>
          </div>
        </div>
        <div style="flex:1; min-width:260px">${img}</div>
      </div>

      <div class="hr"></div>

      <section id="comments">
        <h3 style="margin: 0 0 8px">댓글</h3>
        <form id="form-comment" class="row" style="margin-bottom:8px">
          <textarea class="textarea" id="comment-content" placeholder="댓글을 입력하세요" required></textarea>
          <button class="btn ok" type="submit" style="min-width:120px">등록</button>
        </form>
        <div id="comment-list" class="panel"></div>
        <div class="pager">
          <button class="btn" id="c-prev">이전</button>
          <div id="c-page" class="help"></div>
          <button class="btn" id="c-next">다음</button>
        </div>
      </section>
    `;

        // 좋아요(상세)
        const btnLike = root.querySelector("#btn-like");
        const likeBadge = root.querySelector("#like-count");
        btnLike.addEventListener("click", async () => {
            try {
                const r = await jpost(API.like(id), {});
                likeBadge.textContent = `like: ${r.likeCount}`;
                btnLike.textContent = "이미 좋아요";
                btnLike.disabled = true;
            } catch(e){ alert("좋아요 실패:\n"+e.message); }
        });
        if (data.liked) { btnLike.disabled = true; }

        // 댓글 (생략 없이 기존 로직 그대로)
        const state = { page:0, size:10 };
        root.querySelector("#form-comment").addEventListener("submit", async (ev)=>{
            ev.preventDefault();
            const content = root.querySelector("#comment-content").value.trim();
            if (!content) return;
            try {
                await jpost(API.comments.create(id), {content});
                root.querySelector("#comment-content").value = "";
                state.page = 0;
                await loadComments();
            } catch(e){ alert("댓글 등록 실패:\n"+e.message); }
        });
        root.querySelector("#c-prev").addEventListener("click", ()=>{ state.page=Math.max(0,state.page-1); loadComments(); });
        root.querySelector("#c-next").addEventListener("click", ()=>{ state.page=state.page+1; loadComments(); });

        async function loadComments(){
            try{
                const data = await jget(API.comments.list(id, state.page, state.size));
                const box = root.querySelector("#comment-list");
                box.innerHTML = data.content.map(c=>{
                    return `<div class="row" style="justify-content:space-between; align-items:flex-start; padding:6px 0; border-bottom:1px dashed #203044">
            <div style="white-space:pre-wrap">${esc(c.content)}</div>
            <div class="help">${esc(c.createdAtFormatted||"")}</div>
          </div>`;
                }).join("") || `<div class="center help">댓글이 없습니다.</div>`;
                root.querySelector("#c-page").textContent = `page ${data.page+1} / ${Math.max(1,data.totalPages)} (총 ${data.totalElements})`;
                root.querySelector("#c-prev").disabled = data.page<=0;
                root.querySelector("#c-next").disabled = data.page+1 >= Math.max(1,data.totalPages);
            }catch(e){
                alert("댓글 불러오기 실패:\n"+e.message);
            }
        }
        loadComments();

    } catch(e){
        root.innerHTML = `<div class="panel"><div class="center">상세 불러오기 실패<br/><span class="help">${e.message}</span></div></div>`;
    }
}

function renderNew(root){
    root.innerHTML = `
    <div class="panel">
      <h2 style="margin:0 0 8px">게시글 작성</h2>
      <form id="form-new" class="row">
        <label style="flex:1; min-width:220px">
          <div class="help">카테고리</div>
          <select id="n-category" class="select" required>
            <option value="" disabled selected>선택하세요</option>
            ${Object.keys(CATEGORY_LABELS).map(c=>`<option value="${c}">${catLabel(c)}</option>`).join("")}
          </select>
        </label>
        <label style="flex:1; min-width:280px">
          <div class="help">가게 이름</div>
          <input id="n-name" class="input" required maxlength="100" />
        </label>
        <label style="flex:2; min-width:320px">
          <div class="help">주소</div>
          <input id="n-address" class="input" required maxlength="255" />
        </label>
        <label style="flex:1 1 100%">
          <div class="help">설명/홍보글</div>
          <textarea id="n-content" class="textarea" required></textarea>
        </label>
        <label style="flex:1 1 100%">
          <div class="help">대표 이미지 URL (선택)</div>
          <input id="n-img" class="input" maxlength="512" placeholder="https://..." />
        </label>
        <div style="width:100%; display:flex; gap:8px; justify-content:flex-end">
          <a class="btn" href="#/">취소</a>
          <button class="btn primary" type="submit">등록</button>
        </div>
      </form>
    </div>
  `;
    root.querySelector("#form-new").addEventListener("submit", async (ev)=>{
        ev.preventDefault();
        const payload = {
            category: val("#n-category"),
            name:     val("#n-name"),
            address:  val("#n-address"),
            content:  val("#n-content"),
            imgUrl:   val("#n-img")
        };
        try{
            const r = await jpost(API.create(), payload);
            location.hash = `#/post/${r.id}`;
        }catch(e){
            alert("등록 실패:\n" + e.message);
        }
    });
}

function esc(s){ return String(s ?? "").replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m])); }
function val(sel){ return document.querySelector(sel).value.trim(); }
