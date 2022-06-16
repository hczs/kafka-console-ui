<template>
  <div class="content">
    <a-spin :spinning="loading">
      <div class="topic">
        <div id="components-form-topic-advanced-search">
          <a-form
            class="ant-advanced-search-form"
            :form="form"
            @submit="handleSearch"
          >
            <a-row :gutter="24">
              <a-col :span="8">
                <a-form-item :label="`topic`">
                  <a-input
                    placeholder="topic"
                    class="input-w"
                    v-decorator="['topic']"
                    @change="onTopicUpdate"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item :label="`类型`">
                  <a-select
                    class="type-select"
                    v-model="type"
                    placeholder="选择类型"
                    @change="getTopicList"
                  >
                    <a-select-option value="all"> 所有</a-select-option>
                    <a-select-option value="normal"> 普通</a-select-option>
                    <a-select-option value="system"> 系统</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>

              <a-col :span="8" :style="{ textAlign: 'right' }">
                <a-form-item>
                  <a-button type="primary" html-type="submit"> 刷新</a-button>
                  <!--                  <a-button :style="{ marginLeft: '8px' }" @click="handleReset">-->
                  <!--                    重置-->
                  <!--                  </a-button>-->
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>
        <div class="operation-row-button">
          <a-button type="primary" @click="openCreateTopicDialog"
            >新增</a-button
          >
        </div>
        <a-table
          :columns="columns"
          :data-source="filteredData"
          bordered
          row-key="name"
        >
          <div slot="partitions" slot-scope="text, record">
            <a href="#" @click="openPartitionInfoDialog(record.name)"
              >{{ text }}
            </a>
          </div>

          <div slot="internal" slot-scope="text">
            <span v-if="text" style="color: red">是</span><span v-else>否</span>
          </div>

          <div slot="operation" slot-scope="record" v-show="!record.internal">
            <a-popconfirm
              :title="'删除topic: ' + record.name + '？'"
              ok-text="确认"
              cancel-text="取消"
              @confirm="deleteTopic(record.name)"
            >
              <a-button size="small" href="javascript:;" class="operation-btn"
                >删除
              </a-button>
            </a-popconfirm>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openPartitionInfoDialog(record.name)"
              >分区详情
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openAddPartitionDialog(record.name)"
              >增加分区
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openConsumedDetailDialog(record.name)"
              >消费详情
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openTopicConfigDialog(record.name)"
              >属性配置
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openUpdateReplicaDialog(record.name)"
              >变更副本
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openMessageStatsDialog(record.name)"
              >发送统计
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openThrottleDialog(record.name)"
              >限流
            </a-button>
            <a-button
              size="small"
              href="javascript:;"
              class="operation-btn"
              @click="openRealTimeConsumeDialog(record.name)"
              >实时消费
            </a-button>
          </div>
        </a-table>
        <PartitionInfo
          :topic="selectDetail.resourceName"
          :visible="showPartitionInfo"
          @closePartitionInfoDialog="closePartitionInfoDialog"
        ></PartitionInfo>
        <CreateTopic
          :visible="showCreateTopic"
          @closeCreateTopicDialog="closeCreateTopicDialog"
        >
        </CreateTopic>
        <AddPartition
          :visible="showAddPartition"
          :topic="selectDetail.resourceName"
          @closeAddPartitionDialog="closeAddPartitionDialog"
        ></AddPartition>
        <ConsumedDetail
          :visible="showConsumedDetailDialog"
          :topic="selectDetail.resourceName"
          @closeConsumedDetailDialog="closeConsumedDetailDialog"
        >
        </ConsumedDetail>
        <TopicConfig
          :visible="showTopicConfigDialog"
          :topic="selectDetail.resourceName"
          @closeTopicConfigDialog="closeTopicConfigDialog"
        ></TopicConfig>
        <UpdateReplica
          :visible="showUpdateReplicaDialog"
          :topic="selectDetail.resourceName"
          @closeUpdateReplicaDialog="closeUpdateReplicaDialog"
        ></UpdateReplica>
        <ConfigTopicThrottle
          :visible="showThrottleDialog"
          :topic="selectDetail.resourceName"
          @closeThrottleDialog="closeThrottleDialog"
        ></ConfigTopicThrottle>
        <SendStats
          :visible="showSendStatsDialog"
          :topic="selectDetail.resourceName"
          @closeMessageStatsDialog="closeMessageStatsDialog"
        ></SendStats>
        <!-- 稍后写成组件 -->
        <a-modal
          :title="selectDetail.resourceName + ' 实时消费'"
          :visible="showConsumeDialog"
          :width="1000"
          :mask="false"
          :destroyOnClose="true"
          :footer="null"
          :maskClosable="false"
          @cancel="closeConsumeDialog"
        >
          <div class="reveiveDiv">
            <div v-for="(content, index) in receiveContents" :key="index">
              {{ content }}
            </div>
          </div>
        </a-modal>
      </div>
    </a-spin>
  </div>
</template>

<script>
import request from "@/utils/request";
import { KafkaTopicApi } from "@/utils/api";
import notification from "ant-design-vue/es/notification";
import PartitionInfo from "@/views/topic/PartitionInfo";
import CreateTopic from "@/views/topic/CreateTopic";
import AddPartition from "@/views/topic/AddPartition";
import ConsumedDetail from "@/views/topic/ConsumedDetail";
import TopicConfig from "@/views/topic/TopicConfig";
import UpdateReplica from "@/views/topic/UpdateReplica";
import ConfigTopicThrottle from "@/views/topic/ConfigTopicThrottle";
import SendStats from "@/views/topic/SendStats";
import { getClusterInfo } from "@/utils/local-cache";

export default {
  name: "Topic",
  components: {
    PartitionInfo,
    CreateTopic,
    AddPartition,
    ConsumedDetail,
    TopicConfig,
    UpdateReplica,
    ConfigTopicThrottle,
    SendStats,
  },
  data() {
    return {
      queryParam: { type: "normal" },
      data: [],
      columns,
      selectRow: {},
      form: this.$form.createForm(this, { name: "topic_advanced_search" }),
      showUpdateUser: false,
      deleteUserConfirm: false,
      selectDetail: {
        resourceName: "",
        resourceType: "",
        username: "",
      },
      showPartitionInfo: false,
      loading: false,
      showCreateTopic: false,
      showAddPartition: false,
      showConsumedDetailDialog: false,
      showTopicConfigDialog: false,
      showUpdateReplicaDialog: false,
      showThrottleDialog: false,
      showSendStatsDialog: false,
      showConsumeDialog: false,
      filterTopic: "",
      filteredData: [],
      type: "normal",
      ws: {},
      wsUrl: "ws://localhost:7766/consume",
      // 消费到的数据
      receiveContents: [],
      // 当前集群id
      clusterInfoId: "",
    };
  },
  methods: {
    handleSearch(e) {
      e.preventDefault();
      this.getTopicList();
    },
    handleReset() {
      this.form.resetFields();
    },
    getTopicList() {
      Object.assign(this.queryParam, { type: this.type });
      // delete this.queryParam.topic;
      this.loading = true;
      request({
        url: KafkaTopicApi.getTopicList.url,
        method: KafkaTopicApi.getTopicList.method,
        params: this.queryParam,
      }).then((res) => {
        this.loading = false;
        if (res.code == 0) {
          this.data = res.data;
          this.filter();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    deleteTopic(topic) {
      request({
        url: KafkaTopicApi.deleteTopic.url + "?topic=" + topic,
        method: KafkaTopicApi.deleteTopic.method,
      }).then((res) => {
        if (res.code == 0) {
          this.$message.success(res.msg);
          this.getTopicList();
        } else {
          notification.error({
            message: "error",
            description: res.msg,
          });
        }
      });
    },
    onTopicUpdate(input) {
      this.filterTopic = input.target.value;
      this.filter();
    },
    filter() {
      this.filteredData = this.data.filter(
        (e) => e.name.indexOf(this.filterTopic) != -1
      );
    },
    openPartitionInfoDialog(topic) {
      this.selectDetail.resourceName = topic;
      this.showPartitionInfo = true;
    },
    closePartitionInfoDialog() {
      this.showPartitionInfo = false;
    },
    openCreateTopicDialog() {
      this.showCreateTopic = true;
    },
    closeCreateTopicDialog(res) {
      this.showCreateTopic = false;
      if (res.refresh) {
        this.getTopicList();
      }
    },
    openAddPartitionDialog(topic) {
      this.selectDetail.resourceName = topic;
      this.showAddPartition = true;
    },
    closeAddPartitionDialog(res) {
      this.showAddPartition = false;
      if (res.refresh) {
        this.getTopicList();
      }
    },
    openConsumedDetailDialog(topic) {
      this.showConsumedDetailDialog = true;
      this.selectDetail.resourceName = topic;
    },
    closeConsumedDetailDialog() {
      this.showConsumedDetailDialog = false;
    },
    openTopicConfigDialog(topic) {
      this.showTopicConfigDialog = true;
      this.selectDetail.resourceName = topic;
    },
    closeTopicConfigDialog() {
      this.showTopicConfigDialog = false;
    },
    openUpdateReplicaDialog(topic) {
      this.showUpdateReplicaDialog = true;
      this.selectDetail.resourceName = topic;
    },
    closeUpdateReplicaDialog() {
      this.showUpdateReplicaDialog = false;
    },
    openMessageStatsDialog(topic) {
      this.showSendStatsDialog = true;
      this.selectDetail.resourceName = topic;
    },
    closeMessageStatsDialog() {
      this.showSendStatsDialog = false;
    },
    openThrottleDialog(topic) {
      this.showThrottleDialog = true;
      this.selectDetail.resourceName = topic;
    },
    closeThrottleDialog() {
      this.showThrottleDialog = false;
    },
    // 打开实时消费窗口
    openRealTimeConsumeDialog(topic) {
      console.log("openRealTimeConsumeDialog", topic);
      this.showConsumeDialog = true;
      // 初始化窗口信息
      this.receiveContents = [];
      this.selectDetail.resourceName = topic;
      this.ws.send(this.clusterInfoId + "," + topic);
    },
    closeConsumeDialog() {
      this.showConsumeDialog = false;
      // 发送关闭消费的消息
      this.ws.send("close");
    },
    initWebSocket() {
      this.ws = new WebSocket(this.wsUrl);
      this.ws.onopen = () => {
        console.log("WebSocket 连接成功");
      };
      this.ws.onmessage = (event) => {
        // 向 receivesContents 数组中追加数据
        this.receiveContents.push(event.data);
      };
      this.ws.onclose = () => {
        console.log("WebSocket 连接关闭");
      };
      this.ws.onerror = () => {
        console.log("WebSocket 连接错误");
      };
    },
    initClusterInfoId() {
      const clusterInfo = getClusterInfo();
      this.clusterInfoId = clusterInfo.id;
    },
  },
  created() {
    this.getTopicList();
    this.initClusterInfoId();
    this.initWebSocket();
  },
};

const columns = [
  {
    title: "topic",
    dataIndex: "name",
    key: "name",
    width: 300,
  },
  {
    title: "分区数",
    dataIndex: "partitions",
    key: "partitions",
    slots: { title: "partitions" },
    scopedSlots: { customRender: "partitions" },
  },
  {
    title: "内部topic",
    dataIndex: "internal",
    key: "internal",
    slots: { title: "internal" },
    scopedSlots: { customRender: "internal" },
  },
  {
    title: "操作",
    key: "operation",
    scopedSlots: { customRender: "operation" },
    width: 800,
  },
];
</script>

<style scoped>
.topic {
  width: 100%;
  height: 100%;
}

.ant-advanced-search-form {
  padding: 24px;
  background: #fbfbfb;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
}

.ant-advanced-search-form .ant-form-item {
  display: flex;
}

.ant-advanced-search-form .ant-form-item-control-wrapper {
  flex: 1;
}

#components-form-topic-advanced-search .ant-form {
  max-width: none;
  margin-bottom: 1%;
}

#components-form-topic-advanced-search .search-result-list {
  margin-top: 16px;
  border: 1px dashed #e9e9e9;
  border-radius: 6px;
  background-color: #fafafa;
  min-height: 200px;
  text-align: center;
  padding-top: 80px;
}

.input-w {
  width: 400px;
}

.operation-row-button {
  height: 4%;
  text-align: left;
  margin-bottom: 8px;
}

.operation-btn {
  margin-right: 2%;
}

.type-select {
  width: 200px !important;
}

.reveiveDiv {
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  overflow: auto;
  height: 250px;
}
</style>
