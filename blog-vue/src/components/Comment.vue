<template>
    <div name="comment" class="comment">
        <h2>文章评论</h2>

        <!-- 评论列表 -->
        <div v-if="comments.length > 0">
            <div v-for="(comment, index) in comments" :key="comment.publishDate" class="comment-item" :id="'comment-' + comment.floor">
                <span :id="'comment-floor-' + comment.floor">#{{ index + 1 }}楼 </span>
                <strong>{{ comment.anonymousStatus === 0 ? comment.author : '匿名' }}</strong>
                发表于 {{ formatDate(comment.publishDate) }}
                <p>{{ comment.content }}</p>

                <!-- 回复按钮 -->
                <button class="reply-botton" @click="startReply(comment)">回复</button>
                <p v-if="comment.children.length > 0">{{ comment.children.length }}条回复
                    <button @click="toggleExpand(comment)" class="toggle-btn">
                        {{ comment.expanded ? '收起 ▲' : '展开回复 ▼' }}
                    </button>
                </p>
                <!-- 如果有子评论，递归渲染 -->
                <div v-if="comment.children.length > 0" class="children-comments" v-show="comment.expanded">
                    <div v-for="(child, childIndex) in comment.children" :key="child.publishDate" class="comment-item child-comment" :id="'comment-' + child.floor">
                        <span :id="'comment-floor-' + child.floor">#{{ index + 1 }}楼-{{ childIndex + 1 }}</span>
                        <strong>{{ child.anonymousStatus === 0 ? child.author : '匿名' }}</strong>
                        发表于 {{ formatDate(child.publishDate) }}
                        <p>
                            回复给<strong>{{' ' + findCommentById(comments, child.parentId).author + ' ' }}</strong>
                            <span class="floor-number" :id="'reply-target-' + child.floor" @click="scrollToComment(child.parentId)">
                                加载中...
                            </span> 
                        </p>  
                        <p>{{ child.content }}</p>

                        <!-- 回复按钮 -->
                        <button class="reply-botton" @click="startReply(child)">回复</button>
                    </div>
                </div>
            </div>
        </div>
        <p v-else>暂无评论</p>

        <!-- 添加评论表单 -->
        <div class="add-comment">
            <textarea v-model="newComment" placeholder="写下你的评论..." rows="3"></textarea>
            <input v-model="username" placeholder="用户名 (可选，不填则匿名评论)" />
            <!-- 如果正在回复，则显示取消按钮 -->
            <button style="background-color: red !important;" :style="{ visibility: isReplying ? 'visible' : 'hidden' }" @click="cancelReply">取消回复</button>
            <button @click="submitComment" :disabled="isSubmitting">
                {{ isSubmitting ? "提交中..." : "提交评论" }}
            </button>
        </div>
        <!-- 提示信息 -->
        <p v-if="message" :class="{ success: isSuccess, error: !isSuccess }">{{ message }}</p>
    </div>
</template>

<script setup>
    import { ref, onMounted, watch, nextTick } from "vue";
    import { useRoute } from "vue-router";
    import axios from "axios";

    const comments = ref([]); 
    const route = useRoute();
    const newComment = ref("");
    const username = ref("");
    const isSubmitting = ref(false);
    const message = ref("");
    const isSuccess = ref(true);
    const isReplying = ref(false);
    const replyingTo = ref(null);

    // 格式化日期
    const formatDate = (dateStr) => {
        const date = new Date(dateStr);
        return date.toLocaleString();
    };

    // 递归设置评论的楼层数
    let floor = 0;
    const setCommentFloors = (commentsList) => {
        return commentsList.map((comment) => {
            floor += 1;
            comment.floor = floor;

            comment.expanded = false;

            if (comment.children && comment.children.length > 0) {
                comment.children = setCommentFloors(comment.children);
            }

            return comment;
        });
    };

    // 加载评论
    const loadComments = async () => {
        const articleId = route.params.id;
        try {
            const response = await axios.get(`http://localhost:8080/comments/${articleId}`);
            comments.value = setCommentFloors(response.data);
        } catch (error) {
            console.error("Error fetching comments:", error);
        }
    };

    // 提交评论
    const submitComment = async () => {
        if (!newComment.value.trim()) {
            message.value = "评论内容不能为空！";
            isSuccess.value = false;
            return;
        }

        isSubmitting.value = true;
        const articleId = route.params.id;
        const parentId = isReplying.value ? replyingTo.value.commentId : 0;

        try {
            const response = await axios.post("http://localhost:8080/comments", {
                content: newComment.value,
                author: username.value || "匿名",
                anonymousStatus: username.value ? 0 : 1,
                publishDate: new Date(),
                parentId: parentId,
                articleId: articleId,
                status: 'unread',
            });

            if (response.status === 201 && response.data && response.data.content) {
                message.value = "评论提交成功！";
                isSuccess.value = true;
                newComment.value = "";
                username.value = "";
                cancelReply();
            } else {
                throw new Error("提交失败，请稍后重试。");
            }
        } catch (error) {
            console.error("Error submitting comment:", error);
            message.value = "提交失败，请稍后重试。";
            isSuccess.value = false;
        } finally {
            isSubmitting.value = false;
        }

        loadComments();
    };

    // 开始回复
    const startReply = (comment) => {
        isReplying.value = true;
        replyingTo.value = comment;
        newComment.value = "";
        document.querySelector("textarea").placeholder = `回复给 ${comment.author}`;
        document.querySelector("textarea").scrollIntoView({ behavior: "smooth" });
    };

    // 取消回复
    const cancelReply = () => {
        isReplying.value = false;
        replyingTo.value = null;
        newComment.value = "";
        document.querySelector("textarea").placeholder = "写下你的评论...";
    };

    // 点击楼层数快速滚动到相应楼层
    const scrollToComment = (parentCommentId) => {
        const floor = findCommentById(comments.value, parentCommentId).floor;

        const targetComment = document.querySelector(`#comment-${floor}`);


        if (targetComment) {
            targetComment.scrollIntoView({ behavior: "smooth" });
        }
    };

    // 查找评论楼层
    const findCommentById = (commentsList, targetCommentId) => {
        for (const comment of commentsList) {
            if (comment.commentId === targetCommentId) {
                return comment;
            }
            if (comment.children && comment.children.length > 0) {
                const childComment = findCommentById(comment.children, targetCommentId);
                if (childComment !== undefined) {
                    return childComment;
                }
            }
        }
        return undefined;
    };

    // 更新回复目标内容
    const updateReplyTargets = () => {
        comments.value.forEach(comment => {
            if (comment.children) {
                comment.children.forEach(child => {
                    const floor = findCommentById(comments.value, child.parentId).floor;
                    const targetElement = document.querySelector(`#comment-floor-${floor}`);
                    if (targetElement) {
                        const floorNumber = targetElement.textContent;
                        const replyTargetElement = document.querySelector(`#reply-target-${child.floor}`);
                        if (replyTargetElement) {
                            replyTargetElement.textContent = floorNumber;
                        }
                    }
                });
            }
        });
    };

    const toggleExpand = (comment) => {
        comment.expanded = !comment.expanded;
    };

    onMounted(() => {
        loadComments().then(() => {
            nextTick(() => {
                updateReplyTargets();
            });
        });
    });

    watch(comments, () => {
        nextTick(() => {
            //updateReplyTargets();
        });
    });
</script>


<style scoped>
.comment {
    margin: 50px 0px;
    padding: 1% 2%;
    border: 1px solid #ccc;
    border-radius: 4px;
    background-color: #ffffffa7;
}

.comment-item {
    border-bottom: 1px solid #ddd;
    padding: 10px 0;
    font-size: 16px;
    line-height: 1.2em;
}

.comment-item p {
    margin: 5px 0;
}

.add-comment {
    margin-top: 20px;
}

.success {
    color: green;
}

.error {
    color: red;
}

.comment-item {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 5px;
  border: 1px solid #ddd;
}

.children-comments {
  margin-left: 20px;
}

.child-comment {
  background-color: #f1f1f1;
  border-left: 4px solid #007bff;
  margin-top: 10px;
  padding-left: 10px;
}

.add-comment {
  margin-top: 20px;
}

.add-comment textarea {
    width: 60%;
    min-width: 50%;
    height: 150px;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 5px;
}

.add-comment input {
  width: 40%;
  padding: 10px;
  margin-top: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}

.add-comment button {
    margin-left: 20px;
    margin-top: 10px;
    padding: 10px 15px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.add-comment button:disabled {
  background-color: #ccc;
}

.floor-number {
    cursor: pointer;
    color: #007bff;
    text-decoration: underline;
}

.reply-botton {
    padding: 10px 15px;
    background-color: #007bffbd;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.toggle-btn {
    margin-left: 5px;
    padding: 6px;
    /* background-color: orange; */
    color: gray;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}
</style>
