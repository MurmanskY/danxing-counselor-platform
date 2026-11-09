<template>
  <div>
    <section v-if="isManager" class="profile-overview manager-overview">
      <header class="manager-banner">
        <div>
          <p class="eyebrow">COUNSELOR INSIGHTS</p>
          <h1>辅导员画像总览</h1>
          <p>依托 eHall 数据协同，构建辅导员数智画像，洞察育人成果，赋能成长发展。</p>
        </div>
        <div class="manager-motto">
          <span>博学而笃志</span>
          <span>切问而近思</span>
        </div>
      </header>
      <div class="manager-filterbar panel">
        <div class="mf-search">
          <input
            v-model="mgrQuery"
            type="search"
            placeholder="搜索辅导员姓名、学院、研究方向、标签等关键词"
            aria-label="搜索辅导员"
          />
          <button class="primary" @click="mgrQuery = mgrQuery.trim()">搜索</button>
        </div>
        <div class="mf-selects">
          <label
            >学院
            <select v-model="mgrDept" aria-label="按学院筛选">
              <option value="">全部学院</option>
              <option v-for="d in mgrDepartments" :key="d">{{ d }}</option>
            </select></label
          ><label
            >育人领域
            <select v-model="mgrDomain" aria-label="按育人领域筛选">
              <option value="">全部领域</option>
              <option v-for="d in mgrDomains" :key="d">{{ d }}</option>
            </select></label
          ><label
            >辅导员类型
            <select v-model="mgrType" aria-label="按辅导员类型筛选">
              <option value="">全部类型</option>
              <option v-for="t in mgrTypes" :key="t">{{ t }}</option>
            </select></label
          ><label
            >发展阶段
            <select v-model="mgrStage" aria-label="按发展阶段筛选">
              <option value="">全部阶段</option>
              <option v-for="s in mgrStages" :key="s">{{ s }}</option>
            </select></label
          ><label
            >标签
            <select v-model="mgrTag" aria-label="按标签筛选">
              <option value="">全部标签</option>
              <option v-for="t in hotKeywords" :key="t">{{ t }}</option>
            </select></label
          ><button
            class="mf-reset"
            @click="mgrQuery = mgrDept = mgrDomain = mgrType = mgrStage = mgrTag = ''"
          >
            重置
          </button>
        </div>
      </div>
      <div class="manager-kpis">
        <article v-for="k in managerKpis" :key="k.label">
          <span class="mk-icon" :style="{ background: k.bg }">{{ k.icon }}</span>
          <div class="mk-body">
            <strong><CountUp :value="k.value" /><small> {{ k.unit }}</small></strong>
            <span class="mk-label">{{ k.label }}</span>
            <small class="mk-delta" :class="{ up: k.delta > 0 }">
              <template v-if="k.delta > 0"
                >较上月 ↑ +{{ k.delta }}{{ k.deltaUnit }}</template
              >
              <template v-else>较上月 — 持平</template>
            </small>
          </div>
        </article>
      </div>
      <div class="manager-layout">
        <div class="manager-main">
          <div class="manager-list-head">
            <div>
              <h2>辅导员画像列表</h2>
              <span class="muted">共 {{ sortedManaged.length }} 位辅导员</span>
            </div>
            <div class="mlh-actions">
              <div class="view-toggle">
                <button
                  :class="{ selected: viewMode === 'card' }"
                  @click="viewMode = 'card'"
                >
                  ▦ 卡片视图
                </button>
                <button
                  :class="{ selected: viewMode === 'list' }"
                  @click="viewMode = 'list'"
                >
                  ☰ 列表视图
                </button>
              </div>
              <label class="mgr-sort"
                >排序
                <select v-model="mgrSort" aria-label="排序方式">
                  <option value="score">综合指数</option>
                  <option value="years">工作年限</option>
                  <option value="honors">代表性荣誉</option>
                </select></label
              >
            </div>
          </div>
          <div v-if="!sortedManaged.length" class="muted chart-empty">
            没有匹配的辅导员，请调整筛选条件。
          </div>
          <div v-else-if="viewMode === 'card'" class="manager-card-grid">
            <article
              v-for="p in sortedManaged"
              :key="p.id"
              class="manager-profile-card panel"
            >
              <div class="mpc-top">
                <img
                  v-if="profileAvatarUrl(p.id)"
                  class="mpc-avatar avatar-img"
                  :src="profileAvatarUrl(p.id)"
                  :alt="p.title + ' 的头像'"
                /><span v-else class="mpc-avatar">{{ p.title.slice(-1) }}</span>
                <div class="mpc-id">
                  <strong>{{ p.title }}</strong>
                  <span
                    >{{ p.department || "单位未填写"
                    }}<template v-if="p.payload['辅导员类型']"
                      > · {{ p.payload["辅导员类型"] }}</template
                    ></span
                  >
                </div>
                <em class="kind-tag" :class="identityClass(p)">{{
                  identityTag(p)
                }}</em>
              </div>
              <blockquote v-if="p.payload['个人寄语']" class="mpc-motto">
                “{{ p.payload["个人寄语"] }}”
              </blockquote>
              <div v-if="keywordList(p).length" class="mpc-tags">
                <span
                  v-for="(k, i) in keywordList(p)"
                  :key="k"
                  :style="mgrTagStyle(i)"
                  >{{ k }}</span
                >
              </div>
              <div class="mpc-body">
                <div class="mpc-stats">
                  <div>
                    <strong>{{ honorCount(p) }}</strong
                    ><span>代表性荣誉</span>
                  </div>
                  <div>
                    <strong>{{ achievementCount(p) }}</strong
                    ><span>育人成果</span>
                  </div>
                  <div>
                    <strong>{{ p.payload["综合画像指数"] || "—" }}</strong
                    ><span>综合指数</span>
                  </div>
                  <div class="mpc-studio">
                    <strong>{{ p.payload["工作室名称"] || "未加入" }}</strong
                    ><span>工作室</span>
                  </div>
                </div>
                <svg
                  class="mini-radar"
                  viewBox="0 0 140 128"
                  role="img"
                  :aria-label="p.title + ' 六维能力雷达图'"
                >
                  <polygon
                    v-for="level in [20, 40, 60, 80, 100]"
                    :key="level"
                    :points="miniPolygon(Array(6).fill(level))"
                    class="radar-grid"
                  />
                  <line
                    v-for="(_, i) in 6"
                    :key="i"
                    x1="70"
                    y1="64"
                    :x2="miniPoint(i, 6).x"
                    :y2="miniPoint(i, 6).y"
                    class="radar-axis"
                  />
                  <polygon
                    :points="miniPolygon(cardRadar(p))"
                    class="radar-area pc-area"
                  />
                  <circle
                    v-for="(v, i) in cardRadar(p)"
                    :key="'pt' + i"
                    :cx="miniValuePoint(i, v).x"
                    :cy="miniValuePoint(i, v).y"
                    r="2.2"
                    class="radar-point"
                  />
                  <text
                    v-for="(d, i) in abilityDimensions"
                    :key="d.key"
                    :x="miniLabel(i).x"
                    :y="miniLabel(i).y"
                    :text-anchor="miniLabel(i).anchor"
                    class="mini-radar-label"
                  >{{ d.label }}</text>
                </svg>
              </div>
              <div class="mpc-foot">
                <span
                  >🕐 最近更新：{{
                    p.payload["画像更新时间"] || "2026-11"
                  }}</span
                >
                <button class="light-button" @click="showRecord(p)">
                  查看详情 →
                </button>
              </div>
            </article>
          </div>
          <div v-else class="manager-list-rows">
            <button
              v-for="p in sortedManaged"
              :key="p.id"
              class="manager-list-row panel"
              @click="showRecord(p)"
            >
              <img
                v-if="profileAvatarUrl(p.id)"
                class="mlr-avatar avatar-img"
                :src="profileAvatarUrl(p.id)"
                :alt="p.title + ' 的头像'"
              /><span v-else class="mlr-avatar">{{ p.title.slice(-1) }}</span>
              <span class="mlr-id">
                <strong>{{ p.title }}</strong>
                <small
                  >{{ p.department || "单位未填写" }} ·
                  {{ p.payload["辅导员类型"] || "辅导员" }}</small
                >
              </span>
              <em class="kind-tag" :class="identityClass(p)">{{
                identityTag(p)
              }}</em>
              <span class="mlr-metric"
                ><strong>{{ p.payload["综合画像指数"] || "—" }}</strong
                >综合指数</span
              >
              <span class="mlr-metric"
                ><strong>{{ honorCount(p) }}</strong>代表性荣誉</span
              >
              <span class="mlr-metric"
                ><strong>{{ achievementCount(p) }}</strong>育人成果</span
              >
              <span class="mlr-arrow">→</span>
            </button>
          </div>
        </div>
        <aside class="manager-side">
          <section class="panel side-panel">
            <div class="side-head">
              <div class="side-tabs">
                <button
                  :class="{ selected: tagDistMode === 'tag' }"
                  @click="tagDistMode = 'tag'"
                >
                  按标签
                </button>
                <button
                  :class="{ selected: tagDistMode === 'domain' }"
                  @click="tagDistMode = 'domain'"
                >
                  按领域
                </button>
              </div>
              <router-link class="side-more" to="/manage/people">更多 ›</router-link>
            </div>
            <div v-if="distSegments.length" class="side-donut-layout">
              <svg
                class="donut-chart side-donut"
                viewBox="0 0 144 144"
                role="img"
                aria-label="画像分布环形图"
              >
                <circle cx="72" cy="72" r="52" class="donut-track" />
                <circle
                  v-for="seg in distSegments"
                  :key="seg.label"
                  cx="72"
                  cy="72"
                  r="52"
                  pathLength="100"
                  class="donut-segment"
                  :style="{ color: seg.color }"
                  :stroke-dasharray="`${seg.percent} ${100 - seg.percent}`"
                  :stroke-dashoffset="-seg.offset"
                />
                <text x="72" y="69" class="donut-total">
                  {{ overviewProfiles.length }}
                </text>
                <text x="72" y="88" class="donut-caption">辅导员</text>
              </svg>
              <ul class="donut-legend side-legend">
                <li v-for="seg in distSegments" :key="seg.label">
                  <span class="legend-dot" :style="{ background: seg.color }"></span>
                  <span>{{ seg.label }}</span>
                  <strong>{{ seg.value }}</strong>
                  <em>{{ Math.round(seg.percent) }}%</em>
                </li>
              </ul>
            </div>
            <p v-else class="muted">暂无分布数据。</p>
          </section>
          <section class="panel side-panel">
            <div class="side-head">
              <div class="side-tabs">
                <button
                  :class="{ selected: distMode === 'age' }"
                  @click="distMode = 'age'"
                >
                  年龄分布
                </button>
                <button
                  :class="{ selected: distMode === 'years' }"
                  @click="distMode = 'years'"
                >
                  工龄分布
                </button>
              </div>
            </div>
            <div v-if="distMode === 'years'" class="combo-chart">
              <svg viewBox="0 0 270 150" class="combo-svg" role="img" aria-label="工龄分布图表">
                <line
                  v-for="g in [0, 50, 100]"
                  :key="g"
                  x1="8"
                  :y1="118 - (g / 100) * 88"
                  x2="252"
                  :y2="118 - (g / 100) * 88"
                  class="combo-grid"
                />
                <text
                  v-for="g in [0, 50, 100]"
                  :key="'g' + g"
                  x="262"
                  :y="121 - (g / 100) * 88"
                  class="combo-ylabel"
                >{{ g }}%</text>
                <rect
                  v-for="c in yearCombo"
                  :key="c.label"
                  :x="c.x"
                  :y="c.barY"
                  width="30"
                  :height="c.barH"
                  rx="4"
                  class="combo-bar"
                />
                <text
                  v-for="c in yearCombo"
                  :key="'v' + c.label"
                  :x="c.cx"
                  :y="c.barY - 5"
                  text-anchor="middle"
                  class="combo-val"
                >{{ c.count }}</text>
                <polyline
                  :points="yearCombo.map((c) => c.cx + ',' + c.lineY).join(' ')"
                  class="combo-line"
                />
                <circle
                  v-for="c in yearCombo"
                  :key="'p' + c.label"
                  :cx="c.cx"
                  :cy="c.lineY"
                  r="3"
                  class="combo-dot"
                />
                <text
                  v-for="c in yearCombo"
                  :key="'l' + c.label"
                  :x="c.cx"
                  y="134"
                  text-anchor="middle"
                  class="combo-x"
                >{{ c.label }}</text>
              </svg>
            </div>
            <p v-else class="muted">暂未采集年龄数据，可先切换查看工龄分布。</p>
          </section>
          <section class="panel side-panel">
            <div class="side-head">
              <h3>学院分布</h3>
              <router-link class="side-more" to="/manage/people">更多 ›</router-link>
            </div>
            <div v-if="teamDepartments.length" class="distribution-list">
              <div v-for="item in teamDepartments.slice(0, 8)" :key="item.name">
                <span>{{ item.name }}</span>
                <span class="distribution-track"
                  ><i :style="{ width: item.percent + '%' }"></i></span
                ><strong>{{ item.count }}</strong>
              </div>
            </div>
            <p v-else class="muted">暂无单位数据。</p>
          </section>
          <section class="panel side-panel">
            <div class="side-head">
              <div class="side-tabs">
                <button
                  v-for="t in recTabs"
                  :key="t.value"
                  :class="{ selected: recTab === t.value }"
                  @click="recTab = t.value"
                >
                  {{ t.label }}
                </button>
              </div>
            </div>
            <div v-if="recTab === 'tags'" class="rec-tags">
              <span
                v-for="(k, i) in hotKeywords.slice(0, 10)"
                :key="k"
                :style="mgrTagStyle(i)"
                >{{ k }}</span
              >
            </div>
            <div v-else-if="recommendVisible.length" class="rec-row">
              <button
                v-for="p in recommendVisible"
                :key="p.id"
                class="rec-item"
                @click="showRecord(p)"
              >
                <img
                  v-if="profileAvatarUrl(p.id)"
                  class="rec-avatar avatar-img"
                  :src="profileAvatarUrl(p.id)"
                  :alt="p.title + ' 的头像'"
                /><span v-else class="rec-avatar">{{ p.title.slice(-1) }}</span>
                <strong>{{ p.title }}</strong>
                <small>{{ p.department || "单位未填写" }}</small>
              </button>
            </div>
            <p v-else class="muted">暂无更多画像可推荐。</p>
            <button
              v-if="recTab !== 'tags' && recommendPool.length > 4"
              class="text-button rec-more"
              @click="recOffset = (recOffset + 4) % recommendPool.length"
            >
              换一批
            </button>
          </section>
        </aside>
      </div>
    </section>
    <section v-else-if="activeProfile" class="profile-overview counselor-overview">
      <header class="counselor-profile-banner">
        <img
          v-if="profileAvatarUrl(activeProfile.id)"
          class="profile-avatar large avatar-img"
          :src="profileAvatarUrl(activeProfile.id)"
          :alt="activeProfile.title + ' 的头像'"
        /><div v-else class="profile-avatar large">{{ activeProfile.title.slice(-1) }}</div>
        <div class="counselor-profile-copy">
          <p class="eyebrow">MY COUNSELOR PROFILE</p>
          <h1>{{ activeProfile.title }}</h1>
          <p>
            {{ activeProfile.department || "单位未填写" }}
            <span>·</span>
            {{ activeProfile.payload["辅导员类型"] || "辅导员" }}
          </p>
          <ul class="profile-facts">
            <li v-if="activeProfile.payload['带生类型']">
              🎓 {{ activeProfile.payload["带生类型"] }}
            </li>
            <li>🕐 工作 {{ experience(activeProfile) }} 年</li>
            <li v-if="counselorSince">⏱ {{ counselorSince }}</li>
          </ul>
          <blockquote v-if="profileMotto" class="profile-motto">
            “{{ profileMotto }}”
          </blockquote>
          <p v-if="profileBio" class="profile-bio">{{ profileBio }}</p>
          <div class="tags">
            <span class="tag status-tag"><i class="status-dot"></i>{{ activeProfile.payload["在岗状态"] || "状态待核验" }}</span>
          </div>
        </div>
        <div class="banner-side">
          <div class="banner-stats">
            <div
              v-for="s in bannerStats"
              :key="s.label"
              class="banner-stat"
              :class="{
                'is-primary': ['荣誉数', '工作年限'].includes(s.label),
                'is-zero': Number(s.value) === 0,
              }"
            >
              <span class="banner-stat-icon">{{ s.icon }}</span>
              <div>
                <span>{{ s.label }}</span>
                <strong><CountUp :value="s.value" /><small> {{ s.unit }}</small></strong>
              </div>
            </div>
          </div>
          <button class="light-button" @click="showRecord(activeProfile)">
            查看完整画像 ↗
          </button>
        </div>
      </header>
      <div class="insight-grid">
        <section class="panel insight-panel">
          <div class="insight-head">
            <span class="insight-icon">✓</span>
            <h2>画像雷达</h2>
            <p>六维数智画像，百分制展示</p>
            <span v-if="portraitType" class="tag portrait-tag">
              {{ portraitType }} · {{ portraitScore }} 分
            </span>
          </div>
          <svg
            class="radar-chart"
            viewBox="0 22 280 246"
            role="img"
            :aria-label="activeProfile.title + ' 综合能力画像雷达图'"
          >
            <polygon
              v-for="level in [20, 40, 60, 80, 100]"
              :key="level"
              :points="pcPolygon(Array(abilityScores.length).fill(level))"
              class="radar-grid"
            />
            <line
              v-for="(item, index) in abilityScores"
              :key="item.key"
              x1="140"
              y1="134"
              :x2="pcPoint(100, index).x"
              :y2="pcPoint(100, index).y"
              class="radar-axis"
            />
            <defs>
              <linearGradient id="pcRadarFill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#4a9bf5" stop-opacity="0.45" />
                <stop offset="100%" stop-color="#1b5cb8" stop-opacity="0.16" />
              </linearGradient>
            </defs>
            <polygon :points="pcPolygon(abilityScores.map((s) => s.score))" class="radar-area pc-area" />
            <circle
              v-for="(item, index) in abilityScores"
              :key="item.key"
              :cx="pcPoint(item.score, index).x"
              :cy="pcPoint(item.score, index).y"
              r="3.5"
              class="radar-point"
            />
            <text
              v-for="(item, index) in abilityScores"
              :key="item.key"
              :x="pcLabelPosition(index).x"
              :y="pcLabelPosition(index).y"
              :text-anchor="pcLabelPosition(index).anchor"
              class="radar-label"
            >{{ item.label }}</text>
            <text
              v-for="(item, index) in abilityScores"
              :key="item.key + '-score'"
              :x="pcLabelPosition(index).x"
              :y="pcLabelPosition(index).y + 15"
              :text-anchor="pcLabelPosition(index).anchor"
              class="radar-score"
            >{{ item.score }}</text>
          </svg>
        </section>
        <section class="panel insight-panel">
          <div class="insight-head">
            <span class="insight-icon">✦</span>
            <h2>核心标签</h2>
            <p>基于工作表现与成果的个性化标签</p>
          </div>
          <div v-if="coreTags.length" class="tag-cloud">
            <span
              v-for="(item, i) in cloudItems"
              :key="i"
              :class="item.leaf ? 'cloud-leaf' : 'cloud-tag'"
              :style="item.leaf ? null : tagStyle(item.index)"
              >{{ item.leaf ? "🌱" : item.word }}</span
            >
          </div>
          <p v-else class="muted chart-empty">待补充核心标签。</p>
        </section>
        <section class="panel insight-panel">
          <div class="insight-head">
            <span class="insight-icon">👤</span>
            <h2>成长轨迹</h2>
            <p>记录从学生成长为辅导员以来的重要经历</p>
          </div>
          <ol v-if="growthTimeline.length" class="timeline">
            <li v-for="(item, index) in growthTimeline" :key="index">
              <span class="timeline-period" :style="{ color: stageStyle(item.stage).color }">{{ item.period }}</span>
              <span class="timeline-dot" :style="{ boxShadow: '0 0 0 3px ' + stageStyle(item.stage).bg }"></span>
              <div class="timeline-body">
                <span class="timeline-stage" :style="stageStyle(item.stage)">{{ stageIcon(item.stage) }} {{ item.stage }}</span>
                <strong>{{ item.title }}</strong>
                <p v-if="item.desc">{{ item.desc }}</p>
              </div>
            </li>
          </ol>
          <p v-else class="muted chart-empty">待补充成长经历。</p>
        </section>
      </div>
    </section>
    <section v-else class="profile-overview empty">
      <h2>账号尚未关联辅导员画像</h2>
      <p>请联系管理员辅导员在账号管理中关联你的个人画像。</p>
    </section>
    <section v-if="!isManager && activeProfile" class="personal-showcase">
      <div class="showcase-panel panel">
        <div class="showcase-head">
          <span class="showcase-icon gold">🏆</span>
          <h2>代表性荣誉</h2>
          <p>展示个人获得的重要荣誉奖项</p>
          <router-link to="/honors">查看更多 ›</router-link>
        </div>
        <div v-if="linkedHonors.length" class="honor-grid">
          <article v-for="h in linkedHonors.slice(0, 4)" :key="h.id" class="honor-card">
            <span class="honor-trophy">🏆</span>
            <div>
              <span class="honor-year">{{ honorYear(h) }}</span>
              <strong>{{ h.title }}</strong>
              <small>{{
                h.payload["光荣册历史单位"] || h.department || "授予单位待补充"
              }}</small>
            </div>
          </article>
        </div>
        <p v-else class="muted chart-empty">暂无关联荣誉记录。</p>
      </div>
      <div class="showcase-panel panel">
        <div class="showcase-head">
          <span class="showcase-icon orange">🎓</span>
          <h2>育人成果</h2>
          <p>在工作中形成的典型成果</p>
          <router-link to="/resources">查看更多 ›</router-link>
        </div>
        <div class="stat-grid">
          <div
            v-for="s in outcomeStats"
            :key="s.label"
            class="stat-card"
            :class="{ 'is-zero': Number(s.value) === 0 }"
          >
            <span class="stat-icon">{{ s.icon }}</span>
            <div>
              <strong><CountUp :value="s.value" /></strong>
              <span>{{ s.label }}</span>
              <small>{{ s.sub }}</small>
              <span class="stat-bar"><i :style="{ width: barWidth(s.value) + '%' }"></i></span>
            </div>
          </div>
        </div>
      </div>
      <div class="showcase-panel panel">
        <div class="showcase-head">
          <span class="showcase-icon blue">🏠</span>
          <h2>工作室与资源</h2>
          <p>参与的工作室及主要资源</p>
          <router-link to="/resources">查看更多 ›</router-link>
        </div>
        <div v-if="studioView" class="studio-block">
          <div class="studio-title-row">
            <span class="studio-sprout">🌱</span>
            <strong>{{ studioView.name }}</strong>
            <span v-if="studioRole" class="tag">{{ studioRole }}</span>
          </div>
          <p class="studio-direction">方向：{{ studioView.direction }}</p>
          <p v-if="studioView.desc" class="studio-desc">
            {{ studioView.desc }}
          </p>
          <ul class="studio-stats">
            <li v-for="r in studioResourceStats" :key="r.label">
              <span class="stat-icon small">{{ r.icon }}</span>
              <span>{{ r.label }}</span>
              <strong>{{ r.value }}<small> {{ r.unit }}</small></strong>
            </li>
          </ul>
        </div>
        <p v-else class="muted chart-empty">
          暂未关联工作室，可在画像「工作室名称」等字段填写参与的工作室。
        </p>
      </div>
    </section>
    <section v-if="!isManager && activeProfile" class="network-insight">
      <div class="showcase-panel panel network-panel">
        <div class="showcase-head">
          <span class="showcase-icon blue">👥</span>
          <h2>协作网络</h2>
          <p>与学生、教师、单位的协同育人网络</p>
        </div>
        <div class="network-body">
          <div class="network-graph">
            <svg viewBox="0 0 400 250" class="network-svg" role="img" aria-label="协同育人网络图">
              <defs>
                <clipPath id="netAvatarClip">
                  <circle cx="200" cy="125" r="32" />
                </clipPath>
              </defs>
              <line
                v-for="n in networkNodes"
                :key="n.label"
                x1="200"
                y1="125"
                :x2="n.x"
                :y2="n.y"
                class="net-line"
              />
              <g v-for="(n, i) in networkNodes" :key="'node-' + n.label">
                <circle :cx="n.x" :cy="n.y" r="23" :fill="nodeColor(i)" />
                <text :x="n.x" :y="n.y + 6" class="net-emoji" text-anchor="middle">{{ nodeEmoji(i) }}</text>
                <text :x="n.x" :y="n.y + 40" class="net-label" text-anchor="middle">{{ n.label }}</text>
                <text :x="n.x" :y="n.y + 56" class="net-value" text-anchor="middle">
                  <tspan>{{ n.value }}</tspan> <tspan class="net-unit">{{ n.unit }}</tspan>
                </text>
              </g>
              <circle cx="200" cy="125" r="36" fill="#ffffff" stroke="#dcebff" stroke-width="3" />
              <image
                v-if="networkAvatar"
                :href="networkAvatar"
                x="168"
                y="93"
                width="64"
                height="64"
                clip-path="url(#netAvatarClip)"
                preserveAspectRatio="xMidYMid slice"
              />
              <text
                v-else
                x="200"
                y="136"
                text-anchor="middle"
                class="net-center-fallback"
              >{{ activeProfile.title.slice(-1) }}</text>
              <text x="200" y="182" text-anchor="middle" class="net-name">{{ activeProfile.title }}</text>
            </svg>
          </div>
          <div class="network-legend">
            <h3>主要协作领域</h3>
            <ul>
              <li v-for="(f, i) in focusAreas" :key="f">
                <i :style="{ background: legendColor(i) }"></i>{{ f }}
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="showcase-panel panel insight-ai-panel">
        <div class="showcase-head">
          <span class="showcase-icon purple">💠</span>
          <h2>智能洞察</h2>
          <p>基于画像数据的解读与发展建议</p>
          <span class="tag ai-tag">AI 生成</span>
        </div>
        <div class="ai-body">
          <div class="ai-review">
            <h3>综合评价</h3>
            <p>{{ aiReview }}</p>
            <div class="ai-chips">
              <span v-for="c in aiChips" :key="c" class="ai-chip">{{ c }}</span>
            </div>
          </div>
          <div class="ai-traits">
            <h3>发展特征</h3>
            <ul>
              <li v-for="t in aiTraits" :key="t"><i>✓</i>{{ t }}</li>
            </ul>
          </div>
        </div>
      </div>
    </section>
    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">致每一位用心育人的你</p>
        <h1>辅导员荣誉<br />与职业发展导航</h1>
        <p>
          让育人积累被看见，让专业成长有路径。<br />在旦行，发现你的下一种可能。
        </p>
        <div v-if="!isManager" class="hero-actions">
          <router-link class="primary" to="/routes"
            >探索成长路线 <span>↗</span></router-link
          ><router-link class="light-button" to="/manage/plans"
            >我的发展计划 →</router-link
          >
        </div>
        <div class="hero-note">
          <span class="tiny-dot"></span>以事实为基础，以成长为方向
        </div>
      </div>
      <div class="hero-visual">
        <span class="round-label">育人有方<br />成长有路</span
        ><img src="/danxing-campus-v3.png" alt="绿树环绕的校园建筑" />
        <div class="hero-caption">YOUR NEXT CHAPTER STARTS HERE</div>
      </div>
    </section>
    <section class="metrics">
      <div v-for="m in metrics" :key="m.label">
        <span class="metric-icon" :style="{ background: m.bg }">{{ m.icon }}</span
        ><span>{{ m.label }}</span
        ><strong><CountUp :value="m.value" /><small> 条</small></strong>
        <p>{{ m.note }}</p>
      </div>
    </section>
    <div v-if="!isManager" class="home-columns">
      <section class="panel">
        <div class="section-head">
          <div>
            <p class="eyebrow">MY NEXT STEP</p>
            <h2>近期成长安排</h2>
          </div>
          <router-link to="/calendar">查看日历 ↗</router-link>
        </div>
        <div v-if="upcoming.length">
          <button
            class="event-line"
            v-for="e in upcoming"
            :key="e.id"
            @click="newCalendar(e)"
          >
            <span class="event-date">{{ e.startDate.slice(5) }}</span
            ><span
              ><strong>{{ e.title }}</strong
              ><small>{{ e.category }}</small></span
            ><span>↗</span>
          </button>
        </div>
        <div v-else class="welcome-task">
          <span class="line-art">✧</span>
          <div>
            <h3>为下一步成长，留出时间</h3>
            <p>从梳理一份经历、读懂一个案例开始。</p>
            <button
              class="text-button"
              @click="newCalendar({ title: '梳理个人成长档案' })"
            >
              添加第一条日程 →
            </button>
          </div>
        </div>
        <div class="tip">以上为个人近期安排；官方申报与赛事日期请查看发展日历。</div>
      </section>
    </div>
    <section class="section">
      <div class="section-head">
        <div>
          <p class="eyebrow">INSPIRATION & PRACTICE</p>
          <h2>在优秀实践中，看见更多可能</h2>
        </div>
        <router-link to="/resources">浏览资源中心 ↗</router-link>
      </div>
      <div class="card-grid">
        <RecordCard v-for="r in featured" :key="r.id" :item="r" />
      </div>
    </section>
    <section class="people-strip">
      <div>
        <p class="eyebrow">GROWING TOGETHER</p>
        <h2>同行的力量</h2>
        <p>认识育人伙伴，发现多元成长路径。</p>
      </div>
      <button v-for="p in people" :key="p.id" @click="showRecord(p)">
        <img
          v-if="profileAvatarUrl(p.id)"
          class="person-avatar avatar-img"
          :src="profileAvatarUrl(p.id)"
          :alt="p.title + ' 的头像'"
        /><span v-else class="person-avatar">{{ p.title.slice(-2) }}</span
        ><strong>{{ p.title }}</strong
        ><small>{{ p.department }}</small>
      </button>
    </section>
  </div>
</template>
<script>
import { state, newCalendar, showRecord, profileAvatarUrl } from "../store";
import RecordCard from "../components/RecordCard.vue";
import CountUp from "../components/CountUp.vue";
export default {
  components: { RecordCard, CountUp },
  data: () => ({
    state,
    mgrQuery: "",
    mgrDept: "",
    mgrDomain: "",
    mgrType: "",
    mgrStage: "",
    mgrTag: "",
    mgrSort: "score",
    viewMode: "card",
    tagDistMode: "tag",
    distMode: "years",
    recTab: "similar",
    recOffset: 0,
  }),
  computed: {
    isManager() {
      return state.user.permissions?.includes("VIEW_TEAM_DASHBOARD");
    },
    activeProfile() {
      return state.records.find(
        (record) =>
          record.id === state.dashboard.profileId &&
          record.kind === "person" &&
          record.recordType === "detail",
      );
    },
    managedProfiles() {
      const ids = new Set(state.dashboard.managedProfileIds || []);
      return state.records.filter(
        (record) =>
          ids.has(record.id) &&
          record.kind === "person" &&
          record.recordType === "detail",
      );
    },
    overviewProfiles() {
      const list = [...this.managedProfiles];
      const own = this.activeProfile;
      if (own && !list.some((p) => p.id === own.id)) list.push(own);
      return list;
    },
    allDetailedProfiles() {
      return state.records.filter(
        (record) =>
          record.kind === "person" && record.recordType === "detail",
      );
    },
    radarDimensions() {
      return [
        { key: "experience", label: "工作年限" },
        { key: "honors", label: "荣誉积累" },
        { key: "publications", label: "论文著作" },
        { key: "training", label: "培训经历" },
        { key: "projects", label: "科研项目" },
        { key: "keywords", label: "育人关键词" },
      ];
    },
    teamRadarData() {
      return this.radarDataForProfiles(this.managedProfiles);
    },
    teamEvidenceSegments() {
      return this.evidenceSegments(this.managedProfiles);
    },
    teamEvidenceTotal() {
      return this.teamEvidenceSegments.reduce(
        (sum, segment) => sum + segment.value,
        0,
      );
    },
    activeCounselorCount() {
      return this.overviewProfiles.filter(
        (profile) => profile.payload["在岗状态"] === "在岗",
      ).length;
    },
    managerKpis() {
      const profiles = this.overviewProfiles;
      const excellent = profiles.filter((p) => this.honorCount(p) > 0).length;
      const young = profiles.filter((p) => this.experience(p) <= 5).length;
      const active = this.activeCounselorCount;
      const ratio = profiles.length
        ? Math.round((active / profiles.length) * 1000) / 10
        : 0;
      return [
        { label: "辅导员画像总数", value: profiles.length, unit: "人", icon: "👥", bg: "#e9f2ff", note: "团队全部成员", delta: 1, deltaUnit: "人" },
        { label: "优秀辅导员", value: excellent, unit: "人", icon: "🏆", bg: "#fdf3e0", note: "有代表性荣誉", delta: 1, deltaUnit: "人" },
        { label: "青年辅导员", value: young, unit: "人", icon: "🌱", bg: "#e6f7ee", note: "工作 5 年以内", delta: 1, deltaUnit: "人" },
        { label: "已有关联成果", value: this.teamAchievementCount, unit: "条", icon: "📄", bg: "#f0ebff", note: "荣誉·论文·项目", delta: 6, deltaUnit: "条" },
        { label: "在岗状态", value: active, unit: "人", icon: "🏠", bg: "#e6f6f8", note: "在岗占比 " + ratio + "%", delta: 0, deltaUnit: "人" },
      ];
    },
    mgrDepartments() {
      return [
        ...new Set(this.overviewProfiles.map((p) => p.department).filter(Boolean)),
      ].sort();
    },
    mgrDomains() {
      return [
        ...new Set(this.overviewProfiles.map((p) => p.domain).filter(Boolean)),
      ].sort();
    },
    mgrTypes() {
      return [
        ...new Set(
          this.overviewProfiles.map((p) => p.payload["辅导员类型"]).filter(Boolean),
        ),
      ].sort();
    },
    mgrStages() {
      return [
        ...new Set(
          this.overviewProfiles.map((p) => p.payload["画像类型"]).filter(Boolean),
        ),
      ].sort();
    },
    filteredManaged() {
      const q = this.mgrQuery.trim().toLowerCase();
      return this.overviewProfiles.filter(
        (p) =>
          (!this.mgrDept || p.department === this.mgrDept) &&
          (!this.mgrDomain || p.domain === this.mgrDomain) &&
          (!this.mgrType || p.payload["辅导员类型"] === this.mgrType) &&
          (!this.mgrStage || p.payload["画像类型"] === this.mgrStage) &&
          (!this.mgrTag || this.keywordList(p).includes(this.mgrTag)) &&
          (!q || JSON.stringify(p).toLowerCase().includes(q)),
      );
    },
    sortedManaged() {
      const list = [...this.filteredManaged];
      const score = (p) => Number.parseFloat(p.payload["综合画像指数"]) || 0;
      if (this.mgrSort === "years")
        list.sort((a, b) => this.experience(b) - this.experience(a));
      else if (this.mgrSort === "honors")
        list.sort((a, b) => this.honorCount(b) - this.honorCount(a));
      else list.sort((a, b) => score(b) - score(a));
      return list;
    },
    tagSegments() {
      const counts = {};
      for (const p of this.overviewProfiles)
        for (const k of this.keywordList(p)) counts[k] = (counts[k] || 0) + 1;
      const entries = Object.entries(counts).sort((a, b) => b[1] - a[1]);
      const top = entries.slice(0, 5);
      const rest = entries.slice(5).reduce((s, e) => s + e[1], 0);
      if (rest) top.push(["其他", rest]);
      const total = entries.reduce((s, e) => s + e[1], 0) || 1;
      const colors = ["#2f6fce", "#39c98e", "#d99a2b", "#7a4fd0", "#2fb9a8", "#8ba0b8"];
      let offset = 0;
      return top.map(([label, value], i) => {
        const percent = (value / total) * 100;
        const seg = { label, value, percent, offset, color: colors[i % colors.length] };
        offset += percent;
        return seg;
      });
    },
    tagTotal() {
      return this.tagSegments.reduce((s, seg) => s + seg.value, 0);
    },
    domainSegments() {
      const counts = {};
      for (const p of this.overviewProfiles) {
        const d = p.domain || "未填写";
        counts[d] = (counts[d] || 0) + 1;
      }
      const entries = Object.entries(counts).sort((a, b) => b[1] - a[1]);
      const top = entries.slice(0, 5);
      const rest = entries.slice(5).reduce((s, e) => s + e[1], 0);
      if (rest) top.push(["其他", rest]);
      const total = entries.reduce((s, e) => s + e[1], 0) || 1;
      const colors = ["#2f6fce", "#39c98e", "#d99a2b", "#7a4fd0", "#2fb9a8", "#8ba0b8"];
      let offset = 0;
      return top.map(([label, value], i) => {
        const percent = (value / total) * 100;
        const seg = { label, value, percent, offset, color: colors[i % colors.length] };
        offset += percent;
        return seg;
      });
    },
    distSegments() {
      return this.tagDistMode === "tag" ? this.tagSegments : this.domainSegments;
    },
    hotKeywords() {
      const counts = {};
      for (const p of this.overviewProfiles)
        for (const k of this.keywordList(p)) counts[k] = (counts[k] || 0) + 1;
      return Object.entries(counts)
        .sort((a, b) => b[1] - a[1])
        .map(([k]) => k);
    },
    yearCombo() {
      const buckets = this.yearBuckets;
      const maxCount = Math.max(1, ...buckets.map((b) => b.count));
      const total = buckets.reduce((s, b) => s + b.count, 0) || 1;
      return buckets.map((b, i) => {
        const x = 18 + i * 60;
        const barH = Math.round((b.count / maxCount) * 88);
        const percent = Math.round((b.count / total) * 100);
        return {
          label: b.label,
          count: b.count,
          percent,
          x,
          barH,
          barY: 118 - barH,
          lineY: 118 - Math.round((percent / 100) * 88),
          cx: x + 15,
        };
      });
    },
    yearBuckets() {
      const defs = [
        ["≤3 年", (y) => y <= 3],
        ["4–6 年", (y) => y > 3 && y <= 6],
        ["7–10 年", (y) => y > 6 && y <= 10],
        [">10 年", (y) => y > 10],
      ];
      const buckets = defs.map(([label, test]) => ({
        label,
        count: this.overviewProfiles.filter((p) => test(this.experience(p))).length,
      }));
      const max = Math.max(1, ...buckets.map((b) => b.count));
      return buckets.map((b) => ({ ...b, percent: Math.round((b.count / max) * 100) }));
    },
    recommendPool() {
      const managed = new Set(this.overviewProfiles.map((p) => p.id));
      const pool = this.allDetailedProfiles.filter((p) => !managed.has(p.id));
      if (this.recTab === "young")
        return [...pool].sort((a, b) => this.experience(a) - this.experience(b));
      return pool;
    },
    recTabs() {
      return [
        { value: "similar", label: "相似画像" },
        { value: "young", label: "潜力青年" },
        { value: "tags", label: "关注标签" },
      ];
    },
    recommendVisible() {
      const pool = this.recommendPool;
      if (!pool.length) return [];
      const start = this.recOffset % pool.length;
      const out = [];
      for (let i = 0; i < Math.min(4, pool.length); i++)
        out.push(pool[(start + i) % pool.length]);
      return out;
    },
    averageExperience() {
      if (!this.managedProfiles.length) return "0.0";
      const total = this.managedProfiles.reduce(
        (sum, profile) => sum + this.experience(profile),
        0,
      );
      return (total / this.managedProfiles.length).toFixed(1);
    },
    teamAchievementCount() {
      return this.overviewProfiles.reduce(
        (sum, profile) =>
          sum +
          this.honorCount(profile) +
          this.publicationCount(profile, "发表论文") +
          this.publicationCount(profile, "发表著作") +
          this.entryCount(profile.payload["科研项目"]),
        0,
      );
    },
    teamDepartments() {
      const counts = this.overviewProfiles.reduce((result, profile) => {
        const name = profile.department || "未填写";
        result[name] = (result[name] || 0) + 1;
        return result;
      }, {});
      const max = Math.max(1, ...Object.values(counts));
      return Object.entries(counts)
        .map(([name, count]) => ({
          name,
          count,
          percent: Math.round((count / max) * 100),
        }))
        .sort((a, b) => b.count - a.count);
    },
    teamTypes() {
      const counts = this.managedProfiles.reduce((result, profile) => {
        const name = profile.payload["辅导员类型"] || "未填写";
        result[name] = (result[name] || 0) + 1;
        return result;
      }, {});
      return Object.entries(counts).map(([name, count]) => ({ name, count }));
    },
    metrics() {
      return [
        { label: "个人荣誉", kind: "honor", note: "记录育人路上的闪光", icon: "🏆", bg: "#fff4dc" },
        { label: "集体荣誉", kind: "collective", note: "汇聚共同前行的力量", icon: "🤝", bg: "#e9f2ff" },
        { label: "育人成果", kind: "outcome", note: "分享可借鉴的实践", icon: "🌟", bg: "#e5f8ee" },
        { label: "育人工作室", kind: "studio", note: "连接专业发展伙伴", icon: "🏠", bg: "#f0ebff" },
      ].map((m) => ({
        ...m,
        value: state.records.filter((r) => r.kind === m.kind).length,
      }));
    },
    featured() {
      return [
        ...state.records.filter((r) => r.kind === "outcome").slice(0, 2),
        ...state.records.filter((r) => r.kind === "studio").slice(0, 1),
      ];
    },
    people() {
      return state.records.filter((r) => r.recordType === "detail");
    },
    upcoming() {
      const today = new Date().toLocaleDateString("sv-SE");
      return state.calendar.filter((e) => e.endDate >= today).slice(0, 3);
    },
    abilityDimensions() {
      return [
        { key: "honor", label: "荣誉积累", metric: "honors" },
        { key: "growth", label: "专业成长", metric: "training" },
        { key: "practice", label: "育人实践", metric: "experience" },
        { key: "research", label: "教学研究", metric: "publications" },
        { key: "duty", label: "岗位担当", metric: "keywords" },
        { key: "digital", label: "数智发展", metric: "mentoring" },
      ];
    },
    abilityScores() {
      const profile = this.activeProfile;
      if (!profile) return [];
      const custom = {};
      for (const line of String(profile.payload["能力画像"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean)) {
        const match = line.match(/^(.+?)[\s:：,，]+([0-9]+(?:\.[0-9]+)?)\s*$/);
        if (match) custom[match[1]] = Number.parseFloat(match[2]);
      }
      const indicators = this.profileIndicators(profile);
      indicators.mentoring =
        indicators.honors + indicators.training + indicators.projects;
      return this.abilityDimensions.map(({ key, label, metric }) => {
        const value = custom[label];
        const score = Number.isFinite(value)
          ? value
          : Math.min(98, 55 + indicators[metric] * 8);
        return { key, label, score: Number(score.toFixed(1)) };
      });
    },
    portraitType() {
      return String(this.activeProfile?.payload?.["画像类型"] || "").trim();
    },
    portraitScore() {
      const value = Number.parseFloat(
        this.activeProfile?.payload?.["综合画像指数"],
      );
      return Number.isFinite(value) ? value : null;
    },
    coreTags() {
      const profile = this.activeProfile;
      if (!profile) return [];
      const raw =
        profile.payload["核心标签"] || profile.payload["育人关键词"] || "";
      return String(raw)
        .split(/[、，,;\n]+/)
        .map((word) => word.trim())
        .filter(Boolean);
    },
    cloudItems() {
      const items = this.coreTags.map((word, index) => ({ word, index }));
      if (!items.length) return [];
      items.splice(Math.floor((items.length - 1) / 2) + 1, 0, { leaf: true });
      return items;
    },
    linkedHonorIds() {
      const raw = String(this.activeProfile?.payload?.["历史关联荣誉ID"] || "");
      return (raw.match(/H\d+/g) || []).map(
        (id) => "H" + Number(id.slice(1)),
      );
    },
    linkedHonors() {
      const ids = new Set(this.linkedHonorIds);
      return state.records.filter((r) => r.kind === "honor" && ids.has(r.id));
    },
    outcomeStats() {
      const p = this.activeProfile;
      if (!p) return [];
      const custom = String(p.payload["育人成果统计"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean);
      if (custom.length) {
        return custom.map((line) => {
          const parts = line.split("|").map((s) => s.trim());
          return {
            icon: "📘",
            value: parts[0] || "0",
            label: parts[1] || "",
            sub: parts[2] || "",
          };
        });
      }
      return [
        { icon: "🎓", value: this.publicationCount(p, "发表论文"), label: "发表论文", sub: "含期刊、会议论文" },
        { icon: "📚", value: this.publicationCount(p, "发表著作"), label: "发表著作", sub: "专著与参编教材" },
        { icon: "🔬", value: this.entryCount(p.payload["科研项目"]), label: "育人项目", sub: "科研与育人项目" },
        { icon: "🌱", value: this.entryCount(p.payload["培训经历"]), label: "培训研修", sub: "校内外培训交流" },
      ];
    },
    myStudio() {
      const p = this.activeProfile;
      if (!p) return null;
      const ref = String(p.payload["参与工作室"] || "").trim();
      if (!ref) return null;
      return (
        state.records.find(
          (r) =>
            r.kind === "studio" &&
            (r.id === ref ||
              r.title === ref ||
              (r.payload["工作室名称"] || "") === ref),
        ) || null
      );
    },
    studioView() {
      const p = this.activeProfile;
      if (!p) return null;
      if (this.myStudio) {
        return {
          name: this.myStudio.payload["工作室名称"] || this.myStudio.title,
          direction:
            this.myStudio.payload["工作室方向"] ||
            this.myStudio.domain ||
            "待补充",
          desc: this.myStudio.payload["工作室简介"] || "",
        };
      }
      const name = String(
        p.payload["工作室名称"] || p.payload["参与工作室"] || "",
      ).trim();
      if (!name) return null;
      return {
        name,
        direction: p.payload["工作室方向"] || "待补充",
        desc: p.payload["工作室简介"] || "",
      };
    },
    studioRole() {
      return this.activeProfile?.payload?.["工作室角色"] || "";
    },
    studioResourceStats() {
      const p = this.activeProfile;
      if (!p) return [];
      const custom = String(p.payload["资源统计"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean);
      if (custom.length) {
        return custom.map((line) => {
          const parts = line.split("|").map((s) => s.trim());
          return { icon: "📘", value: parts[0] || "0", label: parts[1] || "", unit: "" };
        });
      }
      return [
        { icon: "🎓", value: this.entryCount(p.payload["培训经历"]), label: "培训交流", unit: "次" },
        { icon: "🔬", value: this.entryCount(p.payload["科研项目"]), label: "研究项目", unit: "项" },
        { icon: "📚", value: this.publicationCount(p, "发表论文") + this.publicationCount(p, "发表著作"), label: "学术资源", unit: "篇" },
        { icon: "🤝", value: this.honorCount(p), label: "荣誉获奖", unit: "项" },
      ];
    },
    counselorSince() {
      const raw = String(
        this.activeProfile?.payload?.["从事辅导员起始年月"] || "",
      ).trim();
      if (!raw || raw === "无") return "";
      return /至今/.test(raw) ? raw : raw + " – 至今";
    },
    profileMotto() {
      return String(this.activeProfile?.payload?.["个人寄语"] || "").trim();
    },
    profileBio() {
      return String(this.activeProfile?.payload?.["个人简介"] || "").trim();
    },
    bannerStats() {
      const p = this.activeProfile;
      if (!p) return [];
      const custom = String(p.payload["横幅统计"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean);
      if (custom.length) {
        const icons = ["🏆", "📄", "👥", "🏠", "📈", "❤️"];
        return custom.map((line, index) => {
          const parts = line.split("|").map((s) => s.trim());
          return {
            icon: icons[index % icons.length],
            label: parts[0] || "",
            value: parts[1] || "0",
            unit: parts[2] || "",
          };
        });
      }
      return [
        { icon: "🏆", label: "荣誉数", value: this.honorCount(p), unit: "项" },
        { icon: "📄", label: "成果数", value: this.publicationCount(p, "发表论文") + this.publicationCount(p, "发表著作"), unit: "项" },
        { icon: "🔬", label: "育人项目", value: this.entryCount(p.payload["科研项目"]), unit: "项" },
        { icon: "🏠", label: "工作室参与", value: this.studioView ? 1 : 0, unit: "个" },
        { icon: "🎓", label: "培训研修", value: this.entryCount(p.payload["培训经历"]), unit: "次" },
        { icon: "🕐", label: "工作年限", value: this.experience(p), unit: "年" },
      ];
    },
    networkNodes() {
      const positions = [
        { x: 88, y: 58 },
        { x: 88, y: 192 },
        { x: 312, y: 58 },
        { x: 312, y: 192 },
      ];
      return String(this.activeProfile?.payload?.["协作网络"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean)
        .slice(0, 4)
        .map((line, i) => {
          const parts = line.split("|").map((s) => s.trim());
          return {
            label: parts[0] || "",
            value: parts[1] || "0",
            unit: parts[2] || "",
            ...positions[i],
          };
        });
    },
    networkAvatar() {
      const id = this.activeProfile?.id;
      return id ? profileAvatarUrl(id) : "";
    },
    focusAreas() {
      return String(this.activeProfile?.payload?.["主要育人领域"] || "")
        .split(/[、，,]/)
        .map((s) => s.trim())
        .filter(Boolean);
    },
    aiReview() {
      const fallback = `${this.activeProfile?.title || "该"}老师坚持育人初心，在各自岗位上稳步成长。`;
      return String(this.activeProfile?.payload?.["综合评价"] || "").trim() || fallback;
    },
    aiTraits() {
      return String(this.activeProfile?.payload?.["发展特征"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean);
    },
    aiChips() {
      const phrase = {
        荣誉积累: "育人成果丰硕",
        专业成长: "学习成长主动",
        育人实践: "实践能力突出",
        教学研究: "教研结合紧密",
        岗位担当: "责任担当过硬",
        数智发展: "数智素养领先",
      };
      const top = [...this.abilityScores]
        .sort((a, b) => b.score - a.score)
        .slice(0, 2)
        .map((d) => phrase[d.label])
        .filter(Boolean);
      return ["育人理念清晰", ...top];
    },
    growthTimeline() {      const profile = this.activeProfile;
      if (!profile) return [];
      const custom = String(profile.payload["成长轨迹"] || "")
        .split(/\n+/)
        .map((line) => line.trim())
        .filter(Boolean);
      if (custom.length) {
        return custom.map((line) => {
          const parts = line.split("|").map((part) => part.trim());
          return {
            period: parts[0] || "",
            stage: parts[1] || "",
            title: parts[2] || "",
            desc: parts[3] || "",
          };
        });
      }
      const items = [];
      for (const line of String(profile.payload["学习经历"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean)) {
        const match = line.match(/^([0-9]{4}[.年-][0-9.\-月至今\s]*?)\s+(.+)$/);
        const period = match ? match[1].trim() : "";
        const rest = match ? match[2].trim() : line;
        const stage = /博士/.test(rest)
          ? "博士阶段"
          : /硕士/.test(rest)
            ? "硕士阶段"
            : /本科/.test(rest)
              ? "本科阶段"
              : "学习经历";
        items.push({ period, stage, title: rest, desc: "" });
      }
      for (const line of String(profile.payload["工作经历"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean)) {
        if (line === "无") continue;
        items.push({ period: "", stage: "工作经历", title: line, desc: "" });
      }
      return items;
    },
  },
  methods: {
    newCalendar,
    showRecord,
    profileAvatarUrl,
    honorYear(honor) {
      return honor.payload?.["奖励时间"] || "";
    },
    stageStyle(stage) {
      const palette = [
        { bg: "#e8f1ff", color: "#2f6fce" },
        { bg: "#f3ecff", color: "#7a4fd0" },
        { bg: "#e7f8f0", color: "#1f9d6c" },
        { bg: "#fff4e5", color: "#d97b1c" },
        { bg: "#ffecec", color: "#d04545" },
        { bg: "#eaf6ff", color: "#1f88c9" },
      ];
      let hash = 0;
      for (const ch of String(stage)) hash = (hash * 31 + ch.charCodeAt(0)) % 997;
      const pick = palette[hash % palette.length];
      return { background: pick.bg, color: pick.color };
    },
    cardRadar(profile) {
      const custom = {};
      for (const line of String(profile.payload["能力画像"] || "")
        .split(/\n+/)
        .map((l) => l.trim())
        .filter(Boolean)) {
        const match = line.match(/^(.+?)[\s:：,，]+([0-9]+(?:\.[0-9]+)?)\s*$/);
        if (match) custom[match[1]] = Number.parseFloat(match[2]);
      }
      const indicators = this.profileIndicators(profile);
      indicators.mentoring =
        indicators.honors + indicators.training + indicators.projects;
      return this.abilityDimensions.map(({ label, metric }) => {
        const value = custom[label];
        return Number.isFinite(value)
          ? value
          : Math.min(98, 55 + indicators[metric] * 8);
      });
    },
    miniPoint(index, total) {
      const angle = -Math.PI / 2 + (index * Math.PI * 2) / total;
      return {
        x: 70 + Math.cos(angle) * 40,
        y: 64 + Math.sin(angle) * 40,
      };
    },
    miniValuePoint(index, value) {
      const point = this.miniPoint(index, 6);
      return {
        x: 70 + (point.x - 70) * (value / 100),
        y: 64 + (point.y - 64) * (value / 100),
      };
    },
    miniLabel(index) {
      const point = this.miniPoint(index, 6);
      const dx = point.x - 70;
      const dy = point.y - 64;
      const len = Math.hypot(dx, dy) || 1;
      return {
        x: Number((70 + (dx / len) * 55).toFixed(1)),
        y: Number((64 + (dy / len) * 55 + 3).toFixed(1)),
        anchor: dx < -6 ? "end" : dx > 6 ? "start" : "middle",
      };
    },
    miniPolygon(scores) {
      return scores
        .map((value, index) => {
          const point = this.miniPoint(index, scores.length);
          const k = value / 100;
          return `${(70 + (point.x - 70) * k).toFixed(1)},${(
            64 +
            (point.y - 64) * k
          ).toFixed(1)}`;
        })
        .join(" ");
    },
    identityTag(profile) {
      if (this.honorCount(profile) >= 3) return "优秀辅导员";
      if (this.experience(profile) <= 5) return "青年辅导员";
      return "骨干辅导员";
    },
    identityClass(profile) {
      if (this.honorCount(profile) >= 3) return "tag-excellent";
      if (this.experience(profile) <= 5) return "tag-young";
      return "tag-backbone";
    },
    mgrTagStyle(index) {
      const palette = [
        { bg: "#e8f1ff", color: "#2f6fce" },
        { bg: "#f3ecff", color: "#7a4fd0" },
        { bg: "#e7f8f0", color: "#1f9d6c" },
        { bg: "#fff4e5", color: "#d97b1c" },
        { bg: "#ffecec", color: "#d04545" },
        { bg: "#eaf6ff", color: "#1f88c9" },
        { bg: "#f0f4ff", color: "#4a5fd0" },
        { bg: "#e9fbff", color: "#1899b4" },
      ];
      const pick = palette[index % palette.length];
      return { background: pick.bg, color: pick.color, fontSize: "11px" };
    },
    achievementCount(profile) {
      return (
        this.publicationCount(profile, "发表论文") +
        this.publicationCount(profile, "发表著作") +
        this.entryCount(profile.payload["科研项目"])
      );
    },
    legendColor(index) {      return ["#e05c5c", "#2fb9a8", "#d99a2b", "#2f6fce", "#1f9d6c", "#7a4fd0"][index % 6];
    },
    nodeColor(index) {
      return ["#e8f1ff", "#e7f8f0", "#f3ecff", "#fff4e5"][index % 4];
    },
    nodeEmoji(index) {
      return ["👥", "👔", "🤝", "🏢"][index % 4];
    },
    stageIcon(stage) {      const s = String(stage);
      if (/学习|博士|硕士|本科/.test(s)) return "🎓";
      if (/团学|党/.test(s)) return "🚩";
      if (/机关|学院|工作|辅导员/.test(s)) return "💼";
      return "📍";
    },
    barWidth(value) {
      const max = Math.max(
        1,
        ...this.outcomeStats.map((s) => Number(s.value) || 0),
      );
      const v = Number(value) || 0;
      return v === 0 ? 0 : Math.max(8, Math.round((v / max) * 100));
    },
    tagStyle(index) {
      const palette = [
        { bg: "#e8f1ff", color: "#2f6fce" },
        { bg: "#f3ecff", color: "#7a4fd0" },
        { bg: "#e7f8f0", color: "#1f9d6c" },
        { bg: "#fff4e5", color: "#d97b1c" },
        { bg: "#ffecec", color: "#d04545" },
        { bg: "#eaf6ff", color: "#1f88c9" },
        { bg: "#f0f4ff", color: "#4a5fd0" },
        { bg: "#e9fbff", color: "#1899b4" },
      ];
      const pick = palette[index % palette.length];
      const size = 13 + ((index * 7) % 3) * 2;
      return { background: pick.bg, color: pick.color, fontSize: size + "px" };
    },
    pcPoint(value, index, factor = 1) {
      const total = this.abilityDimensions.length;
      const angle = -Math.PI / 2 + (index * Math.PI * 2) / total;
      const radius = 80 * (value / 100) * factor;
      return {
        x: Number((140 + Math.cos(angle) * radius).toFixed(2)),
        y: Number((134 + Math.sin(angle) * radius).toFixed(2)),
      };
    },
    pcPolygon(values, factor = 1) {
      return values
        .map((value, index) => this.pcPoint(value, index, factor))
        .map((point) => `${point.x},${point.y}`)
        .join(" ");
    },
    pcLabelPosition(index) {
      const point = this.pcPoint(100, index, 1.3);
      return {
        x: point.x,
        y: Number((point.y + 3).toFixed(2)),
        anchor: point.x < 128 ? "end" : point.x > 152 ? "start" : "middle",
      };
    },
    profileIndicators(profile) {
      return {
        experience: this.experience(profile),
        honors: this.honorCount(profile),
        publications:
          this.publicationCount(profile, "发表论文") +
          this.publicationCount(profile, "发表著作"),
        training: this.entryCount(profile?.payload?.["培训经历"]),
        projects: this.entryCount(profile?.payload?.["科研项目"]),
        keywords: this.keywordList(profile).length,
      };
    },
    radarDataForProfiles(profiles) {
      if (!profiles.length) return [];
      const profileValues = profiles.map((profile) =>
        this.profileIndicators(profile),
      );
      return this.radarDimensions.map(({ key, label }) => {
        const values = profileValues.map((indicators) => indicators[key]);
        const reference = Math.max(
          1,
          ...this.allDetailedProfiles.map(
            (profile) => this.profileIndicators(profile)[key],
          ),
        );
        const value =
          values.reduce((sum, current) => sum + current, 0) / values.length;
        return {
          key,
          label,
          value,
          normalized: Math.min(100, (value / reference) * 100),
        };
      });
    },
    evidenceSegments(profiles) {
      const categories = [
        { label: "荣誉", color: "#e5a528", value: 0 },
        { label: "论文著作", color: "#327fcb", value: 0 },
        { label: "培训", color: "#21a989", value: 0 },
        { label: "科研项目", color: "#8159c9", value: 0 },
      ];
      for (const profile of profiles) {
        const indicators = this.profileIndicators(profile);
        categories[0].value += indicators.honors;
        categories[1].value += indicators.publications;
        categories[2].value += indicators.training;
        categories[3].value += indicators.projects;
      }
      const total = categories.reduce(
        (sum, category) => sum + category.value,
        0,
      );
      let offset = 0;
      return categories.map((category) => {
        const percent = total
          ? (category.value / total) * 100
          : 0;
        const segment = { ...category, percent, offset };
        offset += percent;
        return segment;
      });
    },
    formatMetric(value, key) {
      return key === "experience" ? value.toFixed(1) : Math.round(value);
    },
    radarPoint(value, index, factor = 1) {
      const angle =
        -Math.PI / 2 + (index * Math.PI * 2) / this.radarDimensions.length;
      const radius = 76 * (value / 100) * factor;
      return {
        x: Number((140 + Math.cos(angle) * radius).toFixed(2)),
        y: Number((126 + Math.sin(angle) * radius).toFixed(2)),
      };
    },
    radarAxisEnd(index) {
      return this.radarPoint(100, index);
    },
    radarLabelPosition(index) {
      const point = this.radarPoint(100, index, 1.43);
      return {
        x: point.x,
        y: Number((point.y + 3).toFixed(2)),
        anchor: point.x < 130 ? "end" : point.x > 150 ? "start" : "middle",
      };
    },
    radarPolygon(data, factor = 1) {
      return data
        .map((item, index) =>
          this.radarPoint(item.normalized ?? 100, index, factor),
        )
        .map((point) => `${point.x},${point.y}`)
        .join(" ");
    },
    experience(profile) {
      const value = Number.parseFloat(profile?.payload?.["工作年限"]);
      return Number.isFinite(value) ? value : 0;
    },
    honorCount(profile) {
      const value = profile?.payload?.["历史关联荣誉ID"];
      const ids = String(value || "").match(/H\d+/g) || [];
      return new Set(ids).size;
    },
    entryCount(value) {
      if (!value || value === "无") return 0;
      return String(value)
        .split(/[\n；;]+/)
        .map((entry) => entry.trim())
        .filter(Boolean).length;
    },
    publicationCount(profile, field) {
      return this.entryCount(profile?.payload?.[field]);
    },
    primaryField(profile) {
      return (
        String(profile?.payload?.["主要育人领域"] || "")
          .split(/[、，,]/)
          .map((value) => value.trim())
          .filter(Boolean)[0] || "育人领域待补充"
      );
    },
    keywordList(profile) {
      return String(profile?.payload?.["育人关键词"] || "")
        .split(/[、，,]/)
        .map((value) => value.trim())
        .filter(Boolean);
    },
  },
};
</script>
