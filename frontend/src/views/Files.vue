<template>
  <div>
    <header class="page-heading">
      <p class="eyebrow">MY MATERIALS</p>
      <h1>我的附件</h1>
      <p>留存成长材料，为每一步积累提供依据。</p>
    </header>
    <form class="upload-panel" @submit.prevent="upload">
      <div>
        <h3>上传一份新的材料</h3>
        <p class="muted">
          支持 PDF、Office、图片、TXT、CSV、ZIP，单文件不超过 20 MB。
        </p>
      </div>
      <label
        >关联资料（可选）<select v-model="recordId">
          <option value="">不关联资料</option>
          <option v-for="r in state.records" :key="r.id" :value="r.id">
            {{ r.id }} · {{ r.title }}
          </option>
        </select></label
      ><label
        >选择文件<input
          ref="file"
          type="file"
          accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.png,.jpg,.jpeg,.zip"
          required /></label
      ><button class="primary" :disabled="uploading">
        {{ uploading ? "上传中…" : "上传材料 ↑" }}
      </button>
    </form>
    <p class="notice">
      附件存储在私有 MinIO
      桶中；仅上传账号可以通过鉴权接口下载。资料摘要和原始附件分别管理。
    </p>
    <div v-if="!state.files.length" class="empty">
      <span>▤</span>
      <h3>还没有上传的附件</h3>
      <p>你的学习记录、案例材料和证明文件可以放在这里。</p>
    </div>
    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>文件名称</th>
            <th>大小</th>
            <th>关联资料</th>
            <th>上传日期</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="f in state.files" :key="f.id">
            <td>{{ f.originalName }}</td>
            <td>{{ (f.sizeBytes / 1024).toFixed(1) }} KB</td>
            <td>
              <button
                v-if="f.recordId"
                class="text-button"
                @click="showRecord(f.recordId)"
              >
                {{ f.recordId }}</button
              ><span v-else>—</span>
            </td>
            <td>{{ f.createdAt.slice(0, 10) }}</td>
            <td class="button-row">
              <a :href="'/api/files/' + f.id + '/download'">下载</a
              ><button class="text-button danger" @click="remove(f)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
<script>
import { api } from "../api";
import { state, run, notify, refreshPersonal, showRecord } from "../store";
export default {
  data: () => ({ state, recordId: "", uploading: false }),
  methods: {
    showRecord,
    async upload() {
      const file = this.$refs.file.files[0];
      if (!file) return;
      if (file.size > 20 * 1024 * 1024) {
        notify("文件不能超过 20 MB");
        return;
      }
      this.uploading = true;
      await run(async () => {
        const data = new FormData();
        data.append("file", file);
        if (this.recordId) data.append("recordId", this.recordId);
        await api("/files", "POST", data);
        this.$refs.file.value = "";
        await refreshPersonal();
        notify("材料已上传");
      });
      this.uploading = false;
    },
    async remove(f) {
      if (!confirm("确定删除附件“" + f.originalName + "”？")) return;
      await run(async () => {
        await api("/files/" + f.id, "DELETE");
        await refreshPersonal();
        notify("附件已删除");
      });
    },
  },
};
</script>
