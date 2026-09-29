<template>
  <svg viewBox="0 0 24 24" aria-hidden="true">
    <template v-if="name === 'sysUser'">
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M16 3.2a4 4 0 0 1 0 7.6M22 21v-2a4 4 0 0 0-3-3.5" />
    </template>
    <template v-else-if="name === 'productModel'">
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <path d="M7 9h.01M11 9h.01M15 9h.01M19 9h.01M7 13h.01M11 13h.01M15 13h.01M19 13h.01M8 16h8" />
    </template>
    <template v-else-if="name === 'material'">
      <path d="m12 3 8 4.5-8 4.5-8-4.5L12 3Z" />
      <path d="m4 12 8 4.5 8-4.5M4 16.5l8 4.5 8-4.5" />
    </template>
    <template v-else-if="name === 'productBom'">
      <rect x="9" y="3" width="6" height="4" rx="1" />
      <rect x="3" y="17" width="6" height="4" rx="1" />
      <rect x="15" y="17" width="6" height="4" rx="1" />
      <path d="M12 7v5M6 17v-5h12v5" />
    </template>
    <template v-else-if="name === 'processRoute'">
      <circle cx="5" cy="6" r="2" />
      <circle cx="19" cy="18" r="2" />
      <path d="M7 6h5a3 3 0 0 1 3 3v0a3 3 0 0 1-3 3H9a3 3 0 0 0-3 3v1M16 18h1" />
    </template>
    <template v-else-if="name === 'productionOrder'">
      <rect x="5" y="4" width="14" height="17" rx="2" />
      <path d="M9 4.5V3h6v1.5M9 9h6M9 13h6M9 17h4" />
    </template>
    <template v-else-if="name === 'productionTask'">
      <path d="M9 11l2 2 4-4M4 6h2M4 12h2M4 18h2M9 6h11M9 18h11" />
    </template>
    <template v-else-if="name === 'workReport'">
      <path d="M4 19.5V4a2 2 0 0 1 2-2h9l5 5v12.5a2.5 2.5 0 0 1-2.5 2.5h-11A2.5 2.5 0 0 1 4 19.5Z" />
      <path d="M14 2v6h6M8 17l2-2 2 1 4-4" />
    </template>
    <template v-else-if="name === 'inspectionRecord'">
      <path d="M12 3 4.5 6v5.5c0 4.5 3.2 7.8 7.5 9.5 4.3-1.7 7.5-5 7.5-9.5V6L12 3Z" />
      <path d="m8.5 12 2.2 2.2 4.8-5" />
    </template>
    <template v-else-if="name === 'reworkOrder'">
      <path d="M14.5 6.5a4 4 0 0 0-5-5L7 4l3 3-7.5 7.5a2.1 2.1 0 0 0 3 3L13 10a4 4 0 0 0 5-5l-2.5 2.5-3-3L14.5 2" />
      <path d="m14 14 7 7M18 18l2-2" />
    </template>
    <template v-else-if="name === 'dashboard'">
      <path d="M4 20V10M10 20V4M16 20v-7M22 20H2" />
    </template>
    <template v-else-if="name === 'trace'">
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-4-4M8 8h6M8 11h4M8 14h2" />
    </template>
    <template v-else>
      <path d="M8 6h12M8 12h12M8 18h12M4 6h.01M4 12h.01M4 18h.01" />
    </template>
  </svg>
</template>

<script setup>
defineProps({
  name: {
    type: String,
    required: true
  }
});
</script>
