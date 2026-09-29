<template>
  <section class="mes-login">
    <div class="backdrop-grid" aria-hidden="true"></div>
    <div class="data-streams" aria-hidden="true">
      <span v-for="stream in streamParticles" :key="stream.id" class="stream" :style="stream.style"></span>
    </div>

    <main class="login-layout">
      <section class="intro-panel" aria-labelledby="system-title">
        <header class="brand-lockup">
          <span class="brand-icon">
            <img src="/assets/keyboard-mark.svg" alt="">
          </span>
          <span class="brand-name">KeebWorks <strong>MES</strong></span>
        </header>

        <div class="intro-copy">
          <p class="system-kicker"><span></span> KEYBOARD ASSEMBLY MES</p>
          <h1 id="system-title">定制化键盘<br><em>智能制造执行系统</em></h1>
          <p class="system-description">
            赋能每一把键盘的诞生。从客户订单到精密组装，全流程数字化管理，确保卓越品质与高效交付。
          </p>
        </div>

        <div class="feature-list">
          <article class="feature-item">
            <span class="feature-icon">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 19V9m5 10V5m5 14v-7m5 7V3"/></svg>
            </span>
            <div><h2>实时订单排产</h2><p>从订单下达到产线分配，全链路数据实时同步。</p></div>
          </article>
          <article class="feature-item">
            <span class="feature-icon">
              <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>
            </span>
            <div><h2>全工艺流程追溯</h2><p>组装、焊接与检测数据完整留存，生产过程清晰可查。</p></div>
          </article>
          <article class="feature-item">
            <span class="feature-icon">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 13l5 5L20 7"/></svg>
            </span>
            <div><h2>质量检测与良率分析</h2><p>检验数据自动归集，多维报表辅助质量决策。</p></div>
          </article>
        </div>
      </section>

      <section class="access-column" aria-labelledby="login-title">
        <div class="panel-aura" aria-hidden="true"></div>
        <div class="access-panel">
          <div class="keyboard-banner" aria-hidden="true">
            <div class="keyboard">
              <div v-for="row in 4" :key="row" class="keyboard-row">
                <i v-for="key in 11" :key="key"></i>
              </div>
            </div>
          </div>

          <div class="panel-body">
            <header class="panel-heading">
              <span class="heading-accent"></span>
              <div>
                <h2 id="login-title">操作员登录</h2>
                <p>SECURE ACCESS CONSOLE</p>
              </div>
            </header>

            <div :class="['backend-state', backendStatus]">
              <span :class="['login-status-dot', backendStatus]"></span>
              <div>
                <strong>{{ backendStatusText }}</strong>
                <small>生产系统连接状态</small>
              </div>
            </div>

            <form class="access-form" @submit.prevent="login">
              <label class="field-group">
                <span>EMPLOYEE ID / 工号</span>
                <span class="field-control">
                  <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4 21a8 8 0 0116 0"/></svg>
                  <input
                      v-model.trim="loginForm.employeeNo"
                      type="text"
                      autocomplete="username"
                      placeholder="请输入工号"
                      autofocus
                  >
                </span>
              </label>

              <label class="field-group">
                <span>PASSCODE / 密码</span>
                <span class="field-control">
                  <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="10" width="16" height="11" rx="2"/><path d="M8 10V7a4 4 0 018 0v3"/></svg>
                  <input
                      v-model="loginForm.password"
                      type="password"
                      autocomplete="current-password"
                      placeholder="请输入密码"
                  >
                </span>
              </label>

              <button type="submit" class="login-submit" :disabled="loading.login">
                <span v-if="loading.login" class="button-spinner" aria-hidden="true"></span>
                <span>{{ loading.login ? '身份验证中' : '进入系统' }}</span>
                <svg v-if="!loading.login" viewBox="0 0 24 24" aria-hidden="true"><path d="M5 12h14m-6-6l6 6-6 6"/></svg>
              </button>
            </form>

            <div class="demo-accounts">
              <div class="demo-heading">
                <span>演示账号</span>
                <small>点击即可自动填入</small>
              </div>
              <div class="demo-grid">
                <button type="button" @click="fillDemoAccount('U001', '管理员')"><span>管理员</span><strong>U001</strong></button>
                <button type="button" @click="fillDemoAccount('U002', '计划员')"><span>计划员</span><strong>U002</strong></button>
                <button type="button" @click="fillDemoAccount('U003', '操作员')"><span>操作员</span><strong>U003</strong></button>
                <button type="button" @click="fillDemoAccount('U005', '质检员')"><span>质检员</span><strong>U005</strong></button>
                <button type="button" @click="fillDemoAccount('U007', '返修员')"><span>返修员</span><strong>U007</strong></button>
              </div>
              <p class="demo-password">演示账号统一密码：<strong>123456</strong></p>
            </div>

            <footer :class="['login-feedback', statusType]" aria-live="polite">
              <span :class="['login-status-dot', statusType]"></span>
              <span>{{ statusText }}</span>
            </footer>
          </div>
        </div>

        <div :class="['system-state', backendStatus]">
          <span :class="['login-status-dot', backendStatus]"></span>
          <span>{{ backendStatus === 'ok' ? 'SYSTEM ONLINE | DATA LINK SECURE' : backendStatus === 'warn' ? 'SYSTEM CONNECTION UNAVAILABLE' : 'CHECKING SYSTEM STATUS' }}</span>
        </div>
      </section>
    </main>

    <div class="toast" :class="{ show: toastVisible }" role="status" aria-live="polite">{{ toastMessage }}</div>
  </section>
</template>

<script>
import { sharedOptions } from '../sharedOptions.js';

const loginStreamParticles = Array.from({ length: 15 }, (_, index) => {
  const blurred = Math.random() > 0.5;
  return {
    id: index + 1,
    style: {
      left: `${(Math.random() * 100).toFixed(2)}%`,
      height: `${(10 + Math.random() * 25).toFixed(2)}vh`,
      animationDelay: `${(Math.random() * 5).toFixed(2)}s`,
      filter: blurred ? 'blur(1px)' : 'none',
      '--rise-duration': `${(3 + Math.random() * 4).toFixed(2)}s`,
      '--flicker-duration': `${(2.4 + Math.random() * 2.2).toFixed(2)}s`,
      '--stream-opacity': blurred ? 0.42 : 0.64
    }
  };
});

export default {
  ...sharedOptions,
  computed: {
    ...sharedOptions.computed,
    streamParticles() {
      return loginStreamParticles;
    }
  },
  methods: {
    ...sharedOptions.methods,
    fillDemoAccount(employeeNo, roleName) {
      if (this.loading.login) return;
      this.loginForm.employeeNo = employeeNo;
      this.loginForm.password = '123456';
      this.setStatus(`已填入${roleName}演示账号`, 'ok');
    }
  },
  async mounted() {
    await this.checkBackendConnection();
    await this.loadCurrentUser();
    if (this.authUser) {
      await this.$router.replace(this.defaultRoute());
    }
  }
};
</script>

<style scoped>
:global(body) {
  background: #050a16;
}

.mes-login {
  --login-cyan: #00e7f5;
  --login-blue: #078dc8;
  --login-violet: #8b5cf6;
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  color: #e8f3ff;
  background:
      radial-gradient(circle at 14% 12%, rgba(0, 192, 255, .17), transparent 31%),
      radial-gradient(circle at 88% 90%, rgba(116, 71, 255, .15), transparent 32%),
      linear-gradient(138deg, #08152a 0%, #040914 52%, #091124 100%);
}

.backdrop-grid {
  position: absolute;
  inset: 42% -35% -58%;
  opacity: .18;
  background-image:
      linear-gradient(rgba(58, 184, 235, .3) 1px, transparent 1px),
      linear-gradient(90deg, rgba(58, 184, 235, .3) 1px, transparent 1px);
  background-size: 74px 74px;
  transform: perspective(620px) rotateX(58deg);
  transform-origin: top;
  mask-image: linear-gradient(to bottom, transparent, #000 30%, #000 75%, transparent);
  animation: grid-drift 28s linear infinite;
}
@keyframes grid-drift { to { background-position: 74px 37px; } }

.data-streams { position: absolute; inset: 0; overflow: hidden; pointer-events: none; }
.stream {
  position: absolute;
  bottom: -25vh;
  width: 1px;
  height: 22vh;
  opacity: 0;
  background: linear-gradient(to top, transparent, rgba(0, 231, 245, .4), rgba(226, 253, 255, .66));
  box-shadow: 0 0 9px rgba(0, 231, 245, .34);
  animation:
      stream-rise var(--rise-duration, 5s) linear infinite,
      stream-breathe var(--flicker-duration, 3.4s) ease-in-out infinite alternate;
}
@keyframes stream-rise {
  0% { transform: translateY(0) scaleY(.55); opacity: 0; }
  18%, 72% { opacity: var(--stream-opacity, .48); }
  100% { transform: translateY(-135vh) scaleY(1.2); opacity: 0; }
}
@keyframes stream-breathe {
  0% {
    box-shadow: 0 0 7px rgba(0, 231, 245, .28);
    background: linear-gradient(to top, transparent, rgba(0, 231, 245, .34), rgba(220, 251, 255, .52));
  }
  100% {
    box-shadow: 0 0 15px rgba(0, 231, 245, .58);
    background: linear-gradient(to top, transparent, rgba(0, 231, 245, .56), rgba(240, 255, 255, .88));
  }
}

.login-layout {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(390px, 460px);
  align-items: center;
  gap: clamp(58px, 8vw, 128px);
  width: min(1180px, calc(100% - 64px));
  min-height: 100vh;
  margin: 0 auto;
  padding: 52px 0;
}

.intro-panel { min-width: 0; animation: content-enter .6s ease-out both; }
.brand-lockup { display: flex; align-items: center; gap: 16px; margin-bottom: clamp(48px, 8vh, 82px); }
.brand-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  border: 1px solid rgba(0, 231, 245, .32);
  border-radius: 14px;
  background: rgba(10, 27, 51, .72);
  box-shadow: 0 0 28px rgba(0, 218, 245, .21), inset 0 0 18px rgba(0, 218, 245, .08);
}
.brand-icon img { width: 38px; height: 38px; filter: brightness(0) invert(85%) sepia(83%) saturate(1379%) hue-rotate(130deg); }
.brand-name { color: #f5f9ff; font-size: 27px; font-weight: 750; letter-spacing: .02em; }
.brand-name strong { margin-left: 4px; color: var(--login-cyan); text-shadow: 0 0 14px rgba(0, 231, 245, .62); }

.system-kicker { display: flex; align-items: center; gap: 10px; margin: 0 0 18px; color: #52dce9; font-size: 11px; font-weight: 700; letter-spacing: .28em; }
.system-kicker span { width: 30px; height: 1px; background: var(--login-cyan); box-shadow: 0 0 8px var(--login-cyan); }
.intro-copy h1 { margin: 0; color: #f4f8fd; font-size: 58px; font-weight: 850; line-height: 1.13; letter-spacing: 0; }
.intro-copy h1 em { color: transparent; background: linear-gradient(90deg, #55e6f3, #9a7cff); background-clip: text; font-style: normal; }
.system-description { max-width: 590px; margin: 24px 0 0; color: #9aabc0; font-size: 16px; line-height: 1.85; }

.feature-list { display: grid; gap: 21px; margin-top: 42px; }
.feature-item { display: grid; grid-template-columns: 42px minmax(0, 1fr); gap: 16px; align-items: start; }
.feature-icon { display: grid; place-items: center; width: 42px; height: 42px; border: 1px solid rgba(86, 174, 220, .2); border-radius: 10px; background: rgba(15, 37, 65, .53); transition: border-color .2s, transform .2s; }
.feature-icon svg { width: 20px; fill: none; stroke: var(--login-cyan); stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
.feature-item:hover .feature-icon { border-color: rgba(0, 231, 245, .5); transform: translateY(-2px); }
.feature-item h2 { margin: 0 0 5px; color: #e9f1fa; font-size: 15px; font-weight: 650; }
.feature-item p { margin: 0; color: #6f8199; font-size: 13px; line-height: 1.55; }

.access-column { position: relative; animation: panel-enter .65s .08s ease-out both; }
.panel-aura { position: absolute; inset: -2px; border-radius: 20px; background: linear-gradient(145deg, rgba(0, 231, 245, .65), rgba(131, 84, 246, .55)); filter: blur(10px); opacity: .2; }
.access-panel { position: relative; overflow: hidden; border: 1px solid rgba(149, 184, 220, .26); border-radius: 19px; background: rgba(7, 16, 35, .9); box-shadow: 0 28px 78px rgba(0, 0, 0, .46); backdrop-filter: blur(22px); }
.keyboard-banner { position: relative; height: 118px; overflow: hidden; background: radial-gradient(circle at 50% 0, rgba(0, 199, 237, .27), transparent 61%), linear-gradient(to bottom, rgba(20, 63, 96, .3), #071023); }
.keyboard-banner::after { position: absolute; inset: 0; content: ''; background: linear-gradient(to bottom, transparent 15%, rgba(7, 16, 35, .18), #071023 96%); }
.keyboard { position: absolute; z-index: 1; top: 17px; left: 50%; width: 300px; padding: 13px; border: 1px solid rgba(64, 219, 239, .29); border-radius: 10px; transform: translateX(-50%) perspective(320px) rotateX(54deg); box-shadow: 0 0 34px rgba(0, 206, 239, .15); }
.keyboard-row { display: flex; gap: 5px; margin-bottom: 5px; }
.keyboard-row i { flex: 1; height: 16px; border: 1px solid rgba(123, 225, 241, .26); border-radius: 3px; background: rgba(19, 48, 79, .82); box-shadow: inset 0 0 5px rgba(0, 217, 245, .12); animation: key-breathe 5.6s ease-in-out infinite; }
.keyboard-row:nth-child(2) i { animation-delay: .7s; }
.keyboard-row:nth-child(3) i { animation-delay: 1.4s; }
.keyboard-row:nth-child(4) i { animation-delay: 2.1s; }
@keyframes content-enter { from { opacity: 0; transform: translateY(8px); } }
@keyframes panel-enter { from { opacity: 0; transform: translateY(12px); } }
@keyframes key-breathe { 50% { border-color: rgba(123, 225, 241, .38); background: rgba(22, 57, 90, .88); } }

.panel-body { position: relative; z-index: 2; margin-top: -23px; padding: 0 31px 27px; }
.panel-heading { display: flex; align-items: stretch; gap: 13px; margin-bottom: 20px; }
.heading-accent { width: 4px; border-radius: 3px; background: var(--login-cyan); box-shadow: 0 0 12px rgba(0, 231, 245, .65); }
.panel-heading h2 { margin: 0; color: #fff; font-size: 25px; font-weight: 750; letter-spacing: .02em; }
.panel-heading p { margin: 4px 0 0; color: #687a92; font-size: 10px; letter-spacing: .2em; }

.backend-state { display: flex; align-items: center; gap: 11px; margin-bottom: 19px; padding: 10px 13px; border: 1px solid rgba(126, 158, 192, .16); border-radius: 9px; background: rgba(22, 43, 69, .38); }
.backend-state strong, .backend-state small { display: block; }
.backend-state strong { color: #bed3e3; font-size: 12px; font-weight: 600; }
.backend-state small { margin-top: 2px; color: #5f7189; font-size: 10px; }
.backend-state.ok { border-color: rgba(0, 221, 167, .2); background: rgba(0, 179, 139, .06); }
.backend-state.warn { border-color: rgba(255, 179, 71, .24); background: rgba(255, 157, 45, .06); }

.access-form { display: grid; gap: 17px; }
.field-group { display: grid; gap: 8px; color: #45dce9; font-size: 10px; font-weight: 700; letter-spacing: .12em; }
.field-control { position: relative; display: flex; align-items: center; }
.field-control svg { position: absolute; z-index: 1; left: 15px; width: 18px; fill: none; stroke: #71849c; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; pointer-events: none; transition: stroke .2s; }
.field-control input { height: 50px; border: 1px solid rgba(126, 153, 184, .32); border-radius: 9px; background: rgba(3, 9, 21, .62); color: #f2f8fd; padding: 0 15px 0 45px; font-size: 14px; letter-spacing: normal; transition: border-color .2s, box-shadow .2s, background .2s; }
.field-control input::placeholder { color: #435168; }
.field-control input:focus { border-color: var(--login-cyan); background: rgba(4, 13, 27, .82); box-shadow: 0 0 0 3px rgba(0, 231, 245, .09), 0 0 18px rgba(0, 212, 241, .08); }
.field-control:focus-within svg { stroke: var(--login-cyan); }

.login-submit { display: flex; align-items: center; justify-content: center; gap: 10px; height: 51px; margin-top: 6px; border: 1px solid rgba(87, 225, 246, .44); border-radius: 9px; background: linear-gradient(100deg, #087ead, #00aeca); color: #fff; font-size: 14px; font-weight: 700; letter-spacing: .08em; box-shadow: 0 0 24px rgba(0, 194, 229, .2); transition: transform .2s, box-shadow .2s, filter .2s; }
.login-submit:hover:not(:disabled) { background: linear-gradient(100deg, #0895c7, #05c3da); box-shadow: 0 0 32px rgba(0, 220, 245, .34); transform: translateY(-1px); }
.login-submit svg { width: 18px; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
.login-submit:disabled { background: #126078; color: rgba(255, 255, 255, .72); }
.button-spinner { width: 17px; height: 17px; border: 2px solid rgba(255, 255, 255, .3); border-top-color: #fff; border-radius: 50%; animation: spin .7s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.demo-accounts { display: block; margin-top: 21px; padding: 17px 0 0; border: 0; border-top: 1px solid rgba(108, 136, 168, .18); border-radius: 0; background: transparent; }
.demo-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.demo-heading span { color: #9cabbe; font-size: 11px; font-weight: 650; }
.demo-heading small { color: #53647b; font-size: 9px; }
.demo-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 7px; }
.demo-grid button { display: flex; align-items: center; justify-content: space-between; min-height: 31px; border: 1px solid rgba(109, 145, 181, .16); border-radius: 6px; background: rgba(20, 40, 67, .46); color: #8698ae; padding: 0 10px; font-size: 10px; font-weight: 500; box-shadow: none; transition: border-color .2s, background .2s, color .2s; }
.demo-grid button:hover { border-color: rgba(0, 221, 241, .45); background: rgba(0, 185, 218, .1); color: #d6f7fa; box-shadow: none; }
.demo-grid button strong { color: #c5d9e8; font-family: 'SFMono-Regular', Consolas, monospace; font-size: 10px; }
.demo-password { margin: 9px 0 0; color: #53647b; font-size: 9px; text-align: right; }
.demo-password strong { color: #63cbd7; font-family: 'SFMono-Regular', Consolas, monospace; }

.login-feedback { display: flex; align-items: center; gap: 8px; min-height: 16px; margin-top: 16px; color: #657890; font-size: 10px; }
.login-feedback.ok { color: #70d9bb; }
.login-feedback.warn { color: #e7b66b; }
.login-status-dot { flex: 0 0 auto; width: 8px; height: 8px; border-radius: 50%; background: #64748b; box-shadow: 0 0 0 4px rgba(100, 116, 139, .08); }
.login-status-dot.ok { background: #00dda7; box-shadow: 0 0 10px rgba(0, 221, 167, .76); }
.login-status-dot.warn { background: #ffad45; box-shadow: 0 0 10px rgba(255, 173, 69, .66); }
.login-status-dot.checking { background: #55d8e6; box-shadow: 0 0 10px rgba(85, 216, 230, .68); animation: pulse 1.2s ease-in-out infinite; }
@keyframes pulse { 50% { opacity: .35; transform: scale(.75); } }

.system-state { display: flex; align-items: center; justify-content: center; gap: 9px; margin-top: 18px; color: rgba(82, 211, 227, .67); font-family: 'SFMono-Regular', Consolas, monospace; font-size: 9px; letter-spacing: .1em; }
.system-state.warn { color: rgba(238, 175, 90, .72); }

.toast { z-index: 10; border: 1px solid rgba(98, 218, 231, .22); background: rgba(8, 20, 38, .95); color: #e8f7fb; backdrop-filter: blur(14px); }

@media (max-width: 960px) {
  .login-layout { grid-template-columns: 1fr; width: min(520px, calc(100% - 40px)); gap: 42px; padding: 40px 0; }
.intro-panel { text-align: center; }
  .brand-lockup { justify-content: center; margin-bottom: 35px; }
  .system-kicker { justify-content: center; }
  .system-description { margin-right: auto; margin-left: auto; }
  .feature-list { display: none; }
}

@media (max-width: 520px) {
  .mes-login { overflow: auto; }
  .login-layout { width: min(100% - 24px, 430px); padding: 26px 0 32px; }
  .brand-lockup { margin-bottom: 25px; }
  .brand-icon { width: 48px; height: 48px; }
  .brand-icon img { width: 31px; height: 31px; }
  .brand-name { font-size: 22px; }
  .system-kicker { font-size: 9px; }
  .intro-copy h1 { font-size: 40px; }
  .system-description { margin-top: 16px; font-size: 14px; line-height: 1.7; }
  .keyboard-banner { height: 105px; }
  .panel-body { padding-right: 20px; padding-bottom: 22px; padding-left: 20px; }
  .panel-heading h2 { font-size: 22px; }
  .demo-grid { grid-template-columns: 1fr; }
  .demo-grid button { min-height: 34px; }
  .backend-state, .field-control input, .login-submit { border-radius: 8px; }
  .stream:nth-child(n + 9) { display: none; }
}

</style>
