<template>
  <div class="app-shell">
    <header class="hero">
      <div>
        <p class="eyebrow">LOCK BACKEND TEST UI</p>
        <h1>간단 결재 테스트 프론트</h1>
      </div>
      <button class="ghost-button" @click="refreshAll" :disabled="loading">
        {{ loading ? "불러오는 중..." : "전체 새로고침" }}
      </button>
    </header>

    <section class="tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-button"
        :class="{ active: activeTab === tab.id }"
        @click="activeTab = tab.id"
      >
        {{ tab.label }}
      </button>
    </section>

    <p v-if="globalMessage" class="message success">{{ globalMessage }}</p>
    <p v-if="globalError" class="message error">{{ globalError }}</p>

    <main v-if="activeTab === 'setup'" class="page-grid">
      <section class="panel">
        <div class="panel-head">
          <h2>부서 생성</h2>
          <button class="text-button" @click="fetchDepartments">
            목록 새로고침
          </button>
        </div>
        <form class="form-grid" @submit.prevent="createDepartment">
          <label>
            부서명
            <input
              v-model.trim="departmentForm.name"
              placeholder="예: 개발팀"
              required
            />
          </label>
          <button class="primary-button" type="submit">부서 추가</button>
        </form>

        <div class="list-block">
          <article
            v-for="department in departments"
            :key="department.id"
            class="list-card"
          >
            <div>
              <strong>{{ department.name }}</strong>
              <p>ID: {{ department.id }}</p>
            </div>
            <button
              class="ghost-button small"
              @click="loadBudget(department.id)"
            >
              예산 조회
            </button>
          </article>
          <p v-if="!departments.length" class="empty-text">
            등록된 부서가 없습니다.
          </p>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h2>사용자 생성</h2>
          <button class="text-button" @click="fetchUsers">목록 새로고침</button>
        </div>
        <form class="form-grid" @submit.prevent="createUser">
          <label>
            사번
            <input
              v-model.trim="userForm.employeeNo"
              placeholder="EMP-001"
              required
            />
          </label>
          <label>
            이름
            <input v-model.trim="userForm.name" placeholder="홍길동" required />
          </label>
          <label>
            역할
            <select v-model="userForm.role" required>
              <option v-for="role in roles" :key="role" :value="role">
                {{ role }}
              </option>
            </select>
          </label>
          <label>
            부서
            <select v-model.number="userForm.departmentId" required>
              <option disabled :value="null">부서를 선택하세요</option>
              <option
                v-for="department in departments"
                :key="department.id"
                :value="department.id"
              >
                {{ department.name }} (#{{ department.id }})
              </option>
            </select>
          </label>
          <button
            class="primary-button"
            type="submit"
            :disabled="!departments.length"
          >
            사용자 추가
          </button>
        </form>

        <div class="list-block">
          <article v-for="user in users" :key="user.id" class="list-card">
            <div>
              <strong>{{ user.name }}</strong>
              <p>{{ user.employeeNo }} | {{ user.role }}</p>
              <p>{{ user.departmentName }} (#{{ user.departmentId }})</p>
            </div>
          </article>
          <p v-if="!users.length" class="empty-text">
            등록된 사용자가 없습니다.
          </p>
        </div>
      </section>

      <section class="panel">
        <div class="panel-head">
          <h2>예산 생성/수정</h2>
          <button class="text-button" @click="reloadBudgets">
            전체 예산 새로고침
          </button>
        </div>
        <form class="form-grid" @submit.prevent="upsertBudget">
          <label>
            부서
            <select v-model.number="budgetForm.departmentId" required>
              <option disabled :value="null">부서를 선택하세요</option>
              <option
                v-for="department in departments"
                :key="department.id"
                :value="department.id"
              >
                {{ department.name }} (#{{ department.id }})
              </option>
            </select>
          </label>
          <label>
            총 예산
            <input
              v-model.number="budgetForm.totalAmount"
              type="number"
              min="0"
              required
            />
          </label>
          <label>
            잔여 예산
            <input
              v-model.number="budgetForm.remainingAmount"
              type="number"
              min="0"
              required
            />
          </label>
          <button
            class="primary-button"
            type="submit"
            :disabled="!departments.length"
          >
            저장
          </button>
        </form>

        <div class="list-block">
          <article
            v-for="budget in budgets"
            :key="budget.departmentId"
            class="list-card"
          >
            <div>
              <strong>{{ budget.departmentName }}</strong>
              <p>총액: {{ formatAmount(budget.totalAmount) }}</p>
              <p>잔액: {{ formatAmount(budget.remainingAmount) }}</p>
            </div>
            <button class="ghost-button small" @click="fillBudgetForm(budget)">
              폼에 불러오기
            </button>
          </article>
          <p v-if="!budgets.length" class="empty-text">
            조회된 예산이 없습니다.
          </p>
        </div>
      </section>
    </main>

    <main v-else class="page-grid approval-page">
      <section class="panel wide">
        <div class="panel-head">
          <h2>결재 요청 생성</h2>
          <button class="text-button" @click="fetchApprovals">
            목록 새로고침
          </button>
        </div>
        <form class="form-grid approval-form" @submit.prevent="createApproval">
          <label>
            요청 번호
            <input
              v-model.trim="approvalForm.requestNo"
              placeholder="APR-20260312-001"
              required
            />
          </label>
          <label>
            부서
            <select v-model.number="approvalForm.departmentId" required>
              <option disabled :value="null">부서를 선택하세요</option>
              <option
                v-for="department in departments"
                :key="department.id"
                :value="department.id"
              >
                {{ department.name }} (#{{ department.id }})
              </option>
            </select>
          </label>
          <label>
            요청자
            <select v-model.number="approvalForm.requesterId" required>
              <option disabled :value="null">요청자를 선택하세요</option>
              <option
                v-for="user in filteredUsersByDepartment(
                  approvalForm.departmentId,
                )"
                :key="user.id"
                :value="user.id"
              >
                {{ user.name }} - {{ user.role }} (#{{ user.id }})
              </option>
            </select>
          </label>
          <label>
            승인자
            <select v-model.number="approvalForm.approverId">
              <option :value="null">없음</option>
              <option
                v-for="user in approversByDepartment(approvalForm.departmentId)"
                :key="user.id"
                :value="user.id"
              >
                {{ user.name }} - {{ user.role }} (#{{ user.id }})
              </option>
            </select>
          </label>
          <label>
            제목
            <input
              v-model.trim="approvalForm.title"
              placeholder="노트북 구매 요청"
              required
            />
          </label>
          <label>
            금액
            <input
              v-model.number="approvalForm.amount"
              type="number"
              min="1"
              required
            />
          </label>
          <label class="full">
            설명
            <textarea
              v-model.trim="approvalForm.description"
              rows="3"
              placeholder="상세 사유"
            ></textarea>
          </label>
          <button
            class="primary-button"
            type="submit"
            :disabled="!departments.length || !users.length"
          >
            결재 요청 생성
          </button>
        </form>
      </section>

      <section class="panel wide">
        <div class="panel-head">
          <h2>결재 요청 목록</h2>
          <div class="inline-actions">
            <select v-model="approvalFilter" @change="fetchApprovals">
              <option value="">전체 상태</option>
              <option value="READY">READY</option>
              <option value="APPROVED">APPROVED</option>
              <option value="REJECTED">REJECTED</option>
            </select>
            <button class="ghost-button small" @click="fetchApprovals">
              조회
            </button>
          </div>
        </div>

        <div class="list-block">
          <article
            v-for="approval in approvals"
            :key="approval.id"
            class="approval-card"
          >
            <div class="approval-top">
              <div>
                <strong>{{ approval.title }}</strong>
                <p>{{ approval.requestNo }} | {{ approval.status }}</p>
                <p>
                  {{ approval.departmentName }} / 요청자
                  {{ approval.requesterName }}
                </p>
                <p>승인자: {{ approval.approverName || "미지정" }}</p>
                <p>금액: {{ formatAmount(approval.amount) }}</p>
              </div>
              <button
                class="ghost-button small"
                @click="selectApproval(approval)"
              >
                상세 보기
              </button>
            </div>

            <div
              v-if="approval.status === 'READY' && approval.approverId"
              class="decision-box"
            >
              <label>
                승인 코멘트
                <input
                  v-model.trim="decisionComments[approval.id]"
                  placeholder="선택 입력"
                />
              </label>
              <div class="inline-actions">
                <button
                  class="primary-button small"
                  @click="approveRequest(approval)"
                >
                  승인
                </button>
                <button
                  class="danger-button small"
                  @click="rejectRequest(approval)"
                >
                  반려
                </button>
              </div>
            </div>
          </article>
          <p v-if="!approvals.length" class="empty-text">
            조회된 결재 요청이 없습니다.
          </p>
        </div>
      </section>

      <section class="panel wide" v-if="selectedApproval">
        <div class="panel-head">
          <h2>결재 상세</h2>
          <button
            class="text-button"
            @click="loadApproval(selectedApproval.id)"
          >
            다시 조회
          </button>
        </div>
        <div class="detail-grid">
          <p><strong>ID</strong> {{ selectedApproval.id }}</p>
          <p><strong>요청번호</strong> {{ selectedApproval.requestNo }}</p>
          <p><strong>상태</strong> {{ selectedApproval.status }}</p>
          <p><strong>부서</strong> {{ selectedApproval.departmentName }}</p>
          <p><strong>요청자</strong> {{ selectedApproval.requesterName }}</p>
          <p>
            <strong>승인자</strong>
            {{ selectedApproval.approverName || "미지정" }}
          </p>
          <p>
            <strong>금액</strong> {{ formatAmount(selectedApproval.amount) }}
          </p>
          <p class="full">
            <strong>설명</strong> {{ selectedApproval.description || "-" }}
          </p>
        </div>

        <div class="history-block">
          <h3>히스토리</h3>
          <article
            v-for="history in selectedApproval.histories || []"
            :key="history.id"
            class="history-card"
          >
            <strong>{{ history.action }}</strong>
            <p>{{ history.actorName }} (#{{ history.actorId }})</p>
            <p>{{ history.comment || "-" }}</p>
            <p>{{ history.createdAt }}</p>
          </article>
          <p
            v-if="!(selectedApproval.histories || []).length"
            class="empty-text"
          >
            히스토리가 없습니다.
          </p>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import api from "./api";

const initialDepartmentForm = () => ({
  name: "",
});

const initialUserForm = () => ({
  employeeNo: "",
  name: "",
  role: "USER",
  departmentId: null,
});

const initialBudgetForm = () => ({
  departmentId: null,
  totalAmount: 0,
  remainingAmount: 0,
});

const nextRequestNo = () => {
  const now = new Date();
  const date = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(
    2,
    "0",
  )}${String(now.getDate()).padStart(2, "0")}`;
  const time = `${String(now.getHours()).padStart(2, "0")}${String(
    now.getMinutes(),
  ).padStart(2, "0")}${String(now.getSeconds()).padStart(2, "0")}`;
  return `APR-${date}-${time}`;
};

const initialApprovalForm = () => ({
  requestNo: nextRequestNo(),
  departmentId: null,
  requesterId: null,
  approverId: null,
  title: "",
  description: "",
  amount: 1,
});

export default {
  name: "App",
  data() {
    return {
      tabs: [
        { id: "setup", label: "기초 데이터" },
        { id: "approval", label: "결재 테스트" },
      ],
      activeTab: "setup",
      loading: false,
      globalMessage: "",
      globalError: "",
      approvalFilter: "",
      selectedApproval: null,
      decisionComments: {},
      roles: ["USER", "APPROVER", "ADMIN"],
      departments: [],
      users: [],
      budgets: [],
      approvals: [],
      departmentForm: initialDepartmentForm(),
      userForm: initialUserForm(),
      budgetForm: initialBudgetForm(),
      approvalForm: initialApprovalForm(),
    };
  },
  mounted() {
    this.refreshAll();
  },
  methods: {
    async refreshAll() {
      this.loading = true;
      this.clearNotices();
      try {
        await Promise.all([
          this.fetchDepartments(),
          this.fetchUsers(),
          this.fetchApprovals(),
        ]);
        await this.reloadBudgets();
      } catch (error) {
        this.handleError(error);
      } finally {
        this.loading = false;
      }
    },
    clearNotices() {
      this.globalMessage = "";
      this.globalError = "";
    },
    setMessage(message) {
      this.globalMessage = message;
      this.globalError = "";
    },
    handleError(error) {
      const responseMessage =
        error?.response?.data?.message ||
        error?.response?.data?.error ||
        error?.message ||
        "요청 중 오류가 발생했습니다.";
      this.globalError = responseMessage;
      this.globalMessage = "";
    },
    formatAmount(value) {
      return new Intl.NumberFormat("ko-KR").format(value || 0);
    },
    filteredUsersByDepartment(departmentId) {
      if (!departmentId) {
        return [];
      }
      return this.users.filter((user) => user.departmentId === departmentId);
    },
    approversByDepartment(departmentId) {
      return this.filteredUsersByDepartment(departmentId).filter((user) =>
        ["APPROVER", "ADMIN"].includes(user.role),
      );
    },
    fillBudgetForm(budget) {
      this.budgetForm = {
        departmentId: budget.departmentId,
        totalAmount: budget.totalAmount,
        remainingAmount: budget.remainingAmount,
      };
    },
    selectApproval(approval) {
      this.selectedApproval = approval;
      this.activeTab = "approval";
    },
    async fetchDepartments() {
      const { data } = await api.get("/departments");
      this.departments = data;
      if (!this.userForm.departmentId && data.length) {
        this.userForm.departmentId = data[0].id;
      }
      if (!this.budgetForm.departmentId && data.length) {
        this.budgetForm.departmentId = data[0].id;
      }
      if (!this.approvalForm.departmentId && data.length) {
        this.approvalForm.departmentId = data[0].id;
      }
    },
    async fetchUsers() {
      const { data } = await api.get("/users");
      this.users = data;
      this.syncApprovalActors();
    },
    async fetchApprovals() {
      const params = this.approvalFilter ? { status: this.approvalFilter } : {};
      const { data } = await api.get("/approval-requests", { params });
      this.approvals = data;
      if (this.selectedApproval) {
        const matched = data.find(
          (approval) => approval.id === this.selectedApproval.id,
        );
        this.selectedApproval = matched || this.selectedApproval;
      }
    },
    async loadApproval(id) {
      try {
        const { data } = await api.get(`/approval-requests/${id}`);
        this.selectedApproval = data;
        this.approvals = this.approvals.map((approval) =>
          approval.id === id ? data : approval,
        );
      } catch (error) {
        this.handleError(error);
      }
    },
    async reloadBudgets() {
      if (!this.departments.length) {
        this.budgets = [];
        return;
      }
      const budgetResults = await Promise.allSettled(
        this.departments.map((department) =>
          api.get(`/budgets/department/${department.id}`),
        ),
      );
      this.budgets = budgetResults
        .filter((result) => result.status === "fulfilled")
        .map((result) => result.value.data);
    },
    async loadBudget(departmentId) {
      this.clearNotices();
      try {
        const { data } = await api.get(`/budgets/department/${departmentId}`);
        const index = this.budgets.findIndex(
          (budget) => budget.departmentId === departmentId,
        );
        if (index >= 0) {
          this.budgets.splice(index, 1, data);
        } else {
          this.budgets.push(data);
        }
        this.fillBudgetForm(data);
        this.setMessage(`부서 #${departmentId} 예산을 조회했습니다.`);
      } catch (error) {
        this.handleError(error);
      }
    },
    async createDepartment() {
      this.clearNotices();
      try {
        await api.post("/departments", this.departmentForm);
        this.departmentForm = initialDepartmentForm();
        await this.fetchDepartments();
        await this.reloadBudgets();
        this.setMessage("부서를 생성했습니다.");
      } catch (error) {
        this.handleError(error);
      }
    },
    async createUser() {
      this.clearNotices();
      try {
        await api.post("/users", this.userForm);
        this.userForm = {
          ...initialUserForm(),
          departmentId: this.departments[0]?.id || null,
        };
        await this.fetchUsers();
        this.setMessage("사용자를 생성했습니다.");
      } catch (error) {
        this.handleError(error);
      }
    },
    async upsertBudget() {
      this.clearNotices();
      try {
        const hasExistingBudget = this.budgets.some(
          (budget) => budget.departmentId === this.budgetForm.departmentId,
        );
        const method = hasExistingBudget ? "put" : "post";
        await api[method]("/budgets", this.budgetForm);
        await this.reloadBudgets();
        this.setMessage("예산을 저장했습니다.");
      } catch (error) {
        this.handleError(error);
      }
    },
    syncApprovalActors() {
      const departmentUsers = this.filteredUsersByDepartment(
        this.approvalForm.departmentId,
      );
      if (
        !departmentUsers.find(
          (user) => user.id === this.approvalForm.requesterId,
        )
      ) {
        this.approvalForm.requesterId = departmentUsers[0]?.id || null;
      }
      const departmentApprovers = this.approversByDepartment(
        this.approvalForm.departmentId,
      );
      if (
        !departmentApprovers.find(
          (user) => user.id === this.approvalForm.approverId,
        )
      ) {
        this.approvalForm.approverId = departmentApprovers[0]?.id || null;
      }
    },
    async createApproval() {
      this.clearNotices();
      try {
        await api.post("/approval-requests", this.approvalForm);
        this.approvalForm = {
          ...initialApprovalForm(),
          departmentId: this.approvalForm.departmentId,
        };
        this.syncApprovalActors();
        await this.fetchApprovals();
        await this.reloadBudgets();
        this.setMessage("결재 요청을 생성했습니다.");
        this.activeTab = "approval";
      } catch (error) {
        this.handleError(error);
      }
    },
    async approveRequest(approval) {
      this.clearNotices();
      try {
        await api.post(`/approval-requests/${approval.id}/approve`, {
          actorId: approval.approverId,
          comment: this.decisionComments[approval.id] || "",
        });
        await this.fetchApprovals();
        await this.loadApproval(approval.id);
        await this.reloadBudgets();
        this.setMessage(`결재 요청 #${approval.id} 를 승인했습니다.`);
      } catch (error) {
        this.handleError(error);
      }
    },
    async rejectRequest(approval) {
      this.clearNotices();
      try {
        await api.post(`/approval-requests/${approval.id}/reject`, {
          actorId: approval.approverId,
          comment: this.decisionComments[approval.id] || "",
        });
        await this.fetchApprovals();
        await this.loadApproval(approval.id);
        this.setMessage(`결재 요청 #${approval.id} 를 반려했습니다.`);
      } catch (error) {
        this.handleError(error);
      }
    },
  },
  watch: {
    "approvalForm.departmentId"() {
      this.syncApprovalActors();
    },
  },
};
</script>

<style>
:root {
  --bg: #f3efe6;
  --panel: rgba(255, 251, 245, 0.95);
  --panel-border: rgba(60, 42, 27, 0.12);
  --ink: #26170f;
  --muted: #7a685b;
  --accent: #b85c38;
  --accent-strong: #8a3f21;
  --success: #22543d;
  --success-bg: #e6fffa;
  --danger: #8b1e1e;
  --danger-bg: #fff1f1;
  --shadow: 0 18px 40px rgba(71, 49, 34, 0.12);
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  background: radial-gradient(
      circle at top left,
      rgba(184, 92, 56, 0.18),
      transparent 30%
    ),
    linear-gradient(135deg, #efe4d2, #f8f5ef 45%, #ebe1d0);
  color: var(--ink);
  font-family: "Segoe UI", "Noto Sans KR", sans-serif;
}

button,
input,
select,
textarea {
  font: inherit;
}

#app {
  min-height: 100vh;
}

.app-shell {
  max-width: 1440px;
  margin: 0 auto;
  padding: 32px 20px 48px;
}

.hero,
.panel {
  background: var(--panel);
  border: 1px solid var(--panel-border);
  border-radius: 24px;
  box-shadow: var(--shadow);
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  padding: 28px;
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--accent-strong);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.hero h1 {
  margin: 0;
  font-size: clamp(28px, 4vw, 44px);
}

.hero-copy {
  max-width: 720px;
  margin: 12px 0 0;
  color: var(--muted);
  line-height: 1.6;
}

.tab-bar {
  display: flex;
  gap: 12px;
  margin: 20px 0 16px;
}

.tab-button,
.ghost-button,
.text-button,
.primary-button,
.danger-button {
  border: none;
  border-radius: 999px;
  cursor: pointer;
  transition: transform 0.16s ease, opacity 0.16s ease,
    background-color 0.16s ease;
}

.tab-button,
.ghost-button,
.text-button {
  background: rgba(255, 250, 244, 0.86);
  color: var(--ink);
}

.tab-button {
  padding: 12px 18px;
  font-weight: 700;
}

.tab-button.active {
  background: var(--accent);
  color: #fff8f0;
}

.text-button,
.ghost-button.small,
.primary-button.small,
.danger-button.small {
  padding: 10px 14px;
}

.ghost-button,
.primary-button,
.danger-button {
  padding: 12px 18px;
  font-weight: 700;
}

.primary-button {
  background: var(--accent);
  color: #fff9f2;
}

.danger-button {
  background: var(--danger);
  color: #fff7f7;
}

.ghost-button:hover,
.text-button:hover,
.primary-button:hover,
.danger-button:hover,
.tab-button:hover {
  transform: translateY(-1px);
}

.ghost-button:disabled,
.primary-button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.message {
  margin: 0 0 16px;
  padding: 14px 18px;
  border-radius: 16px;
  font-weight: 600;
}

.message.success {
  background: var(--success-bg);
  color: var(--success);
}

.message.error {
  background: var(--danger-bg);
  color: var(--danger);
}

.page-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.approval-page {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel {
  padding: 22px;
}

.panel.wide {
  grid-column: span 2;
}

.panel-head,
.approval-top,
.inline-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.panel-head {
  margin-bottom: 16px;
}

.panel-head h2,
.history-block h3 {
  margin: 0;
}

.form-grid {
  display: grid;
  gap: 14px;
}

.approval-form {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-grid label,
.decision-box label {
  display: grid;
  gap: 8px;
  color: var(--muted);
  font-size: 14px;
  font-weight: 600;
}

.full {
  grid-column: 1 / -1;
}

input,
select,
textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(89, 67, 52, 0.18);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.88);
}

textarea {
  resize: vertical;
}

.list-block,
.history-block {
  display: grid;
  gap: 12px;
  margin-top: 18px;
}

.list-card,
.approval-card,
.history-card {
  border: 1px solid rgba(89, 67, 52, 0.14);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  padding: 16px;
}

.list-card p,
.approval-card p,
.history-card p,
.detail-grid p {
  margin: 6px 0 0;
  color: var(--muted);
}

.decision-box {
  display: grid;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(89, 67, 52, 0.12);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 18px;
}

.empty-text {
  margin: 4px 0 0;
  color: var(--muted);
}

@media (max-width: 1100px) {
  .page-grid,
  .approval-page {
    grid-template-columns: 1fr;
  }

  .panel.wide {
    grid-column: span 1;
  }
}

@media (max-width: 720px) {
  .app-shell {
    padding: 18px 14px 32px;
  }

  .hero,
  .panel {
    padding: 18px;
    border-radius: 18px;
  }

  .hero,
  .panel-head,
  .approval-top,
  .inline-actions,
  .tab-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .approval-form,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
