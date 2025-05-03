<template>
    <div class="custom-select" @click="toggleDropdown" tabindex="0">
        <div class="select-box" :class="{ 'active': dropdownOpen }">
            <span>{{ selectedOption ? selectedOption.label : placeholder }}</span>
            <!-- <i :class="{'icon-up': dropdownOpen, 'icon-down': !dropdownOpen}"></i> -->
            <i :class="{ 'rotated': dropdownOpen }">
                <svg class="i-icon " width="14" height="8" viewBox="0 0 14 8" xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 1l6 6 6-6" stroke="#999" stroke-width="2" fill="none" />
                </svg>
            </i>
        </div>
        <div v-if="dropdownOpen" class="dropdown">
            <input 
                v-if="searchable" 
                v-model="searchQuery" 
                class="search-input" 
                placeholder="搜索" 
                @click.stop 
            />
            <ul>
                <li 
                    v-for="option in filteredOptions" 
                    :key="option.value" 
                    @click="selectOption(option)" 
                    :class="{ 'selected': option.value === modelValue }"
                >
                    {{ option.label }}
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
export default {
    props: {
        modelValue: {
            type: [String, Number],
            default: null
        },
        options: {
            type: Array,
            required: true,
            default: () => []
        },
        placeholder: {
            type: String,
            default: 'Select an option'
        },
        searchable: {
            type: Boolean,
            default: true
        }
    },
    data() {
        return {
            dropdownOpen: false,
            searchQuery: ''
        };
    },
    computed: {
        filteredOptions() {
            if (!Array.isArray(this.options)) return [];
            return this.searchable
                ? this.options.filter(option =>
                    option.label.toLowerCase().includes(this.searchQuery.toLowerCase())
                )
                : this.options;
        },
        selectedOption() {
            if (!Array.isArray(this.options)) return null;
            return this.options.find(option => option.value === this.modelValue);
        }
    },
    mounted() {
        document.addEventListener('mousedown', this.handleClickOutside);
    },
    unmounted() {
        document.removeEventListener('mousedown', this.handleClickOutside);
    },
    methods: {
        toggleDropdown() {
            this.dropdownOpen = !this.dropdownOpen;
        },
        selectOption(option) {
            this.$emit('update:modelValue', option.value);
            this.$emit('change', option);
            this.dropdownOpen = true;
        },
        handleClickOutside(event) {
            if (!this.$el.contains(event.target)) {
                this.dropdownOpen = false;
            }
        }
    }
};
</script>

<style scoped>
.custom-select {
    position: relative;
    width: 120px;
    font-family: Arial, sans-serif;
    font-size: 14px;
}

.select-box {
    padding: 10px 14px;
    border: 1px solid #e0e0e0;
    border-radius: 10px; /* 超圆润，像 pill 一样 */
    background-color: #f9f9f9;
    color: #333;
    appearance: none;
    background-repeat: no-repeat;
    background-position: right 12px center;
    background-size: 14px 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    outline: none;
}

.dropdown {
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    min-height: 200px;
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid #ccc;
    background-color: white;
    border-radius: 4px;
    margin-top: 5px;
    z-index: 10;
}
.search-input {
    width: 100%;
    padding: 8px;
    border: none;
    border-bottom: 1px solid #ddd;
    margin-bottom: 5px;
    box-sizing: border-box;
}
ul {
    list-style: none;
    padding: 0;
    margin: 0;
}
li {
    padding: 10px;
    cursor: pointer;
}
li:hover {
    background-color: #f0f0f0;
}
.selected {
    background-color: #007bff;
    color: white;
}
.select-box.active {
    border-color: #007bff;
    box-shadow: 0 0 0 1px rgba(0, 123, 255, 0.2);
}

/* 图标定位到右边 */
.select-box i {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    display: inline-block;
    transition: transform 0.3s ease;
}

/* 旋转状态 */
.select-box i.rotated {
    transform: translateY(-50%) rotate(180deg);
}
</style>
