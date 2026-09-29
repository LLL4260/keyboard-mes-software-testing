import { createRouter, createWebHashHistory } from 'vue-router';
import LoginView from './views/LoginView.vue';
import WorkspaceView from './views/WorkspaceView.vue';

const routes = [
  { path: '/', redirect: { name: 'login' } },
  { path: '/login', name: 'login', component: LoginView },
  { path: '/work/:resourceName?', name: 'work', component: WorkspaceView },
  { path: '/reports', name: 'dashboard', component: WorkspaceView },
  { path: '/trace', name: 'trace', component: WorkspaceView },
  { path: '/operations', name: 'operations', component: WorkspaceView },
  { path: '/:pathMatch(.*)*', redirect: { name: 'work', params: { resourceName: 'productionOrder' } } }
];

export const router = createRouter({
  history: createWebHashHistory(),
  routes
});
