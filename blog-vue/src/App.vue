<template>
	<div id="app">
		<router-view :key="$route.path"></router-view>
	</div>
</template>

<script setup>
document.title = '清科谷体的博客 - ';
</script>

<style>
</style>
