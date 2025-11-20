<template>
  <n-config-provider :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <Content />
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { NConfigProvider, NMessageProvider, NDialogProvider, NNotificationProvider, useMessage, zhCN, dateZhCN } from 'naive-ui'
import { defineComponent, h } from 'vue'
import { RouterView } from 'vue-router'

// 创建一个包装组件来提供全局 message
const Content = defineComponent({
  setup() {
    window.$message = useMessage()
    return () => h(RouterView)
  }
})

// 主题配置 - 主题色 #18A058
const themeOverrides = {
  common: {
    primaryColor: '#18A058',
    primaryColorHover: '#36AD6A',
    primaryColorPressed: '#0C7A43',
    primaryColorSuppl: '#36AD6A'
  },
  Button: {
    textColorPrimary: '#fff'
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
