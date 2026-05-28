# SuperBizAgent

> oncall_agent-: 涓€涓?agent 椤圭洰锛屽彲浠ヤ笌 agent 鑱婂ぉ锛屽苟澶勭悊杩愮淮鎶ヨ杩斿洖瑙ｅ喅鏂规硶

> 鍩轰簬 Spring Boot + AI Agent 鐨勬櫤鑳介棶绛斾笌杩愮淮绯荤粺

## 馃摉 椤圭洰绠€浠?

浼佷笟绾ф櫤鑳戒笟鍔′唬鐞嗙郴缁燂紝鍖呭惈涓ゅぇ鏍稿績妯″潡锛?

### 1. RAG 鏅鸿兘闂瓟
闆嗘垚 Milvus 鍚戦噺鏁版嵁搴撳拰闃块噷浜?DashScope锛屾彁渚涘熀浜庢绱㈠寮虹敓鎴愮殑鏅鸿兘闂瓟鑳藉姏锛屾敮鎸佸杞璇濆拰娴佸紡杈撳嚭銆?

### 2. AIOps 鏅鸿兘杩愮淮
鍩轰簬 AI Agent 鐨勮嚜鍔ㄥ寲杩愮淮绯荤粺锛岄噰鐢?Planner-Executor-Replanner 鏋舵瀯锛屽疄鐜板憡璀﹀垎鏋愩€佹棩蹇楁煡璇€佹櫤鑳借瘖鏂拰鎶ュ憡鐢熸垚銆?

## 馃殌 鏍稿績鐗规€?

- 鉁?**RAG 闂瓟**: 鍚戦噺妫€绱?+ 澶氳疆瀵硅瘽 + 娴佸紡杈撳嚭
- 鉁?**AIOps 杩愮淮**: 鏅鸿兘璇婃柇 + 澶?Agent 鍗忎綔 + 鑷姩鎶ュ憡
- 鉁?**宸ュ叿闆嗘垚**: 鏂囨。妫€绱€佸憡璀︽煡璇€佹棩蹇楀垎鏋愩€佹椂闂村伐鍏?
- 鉁?**浼氳瘽绠＄悊**: 涓婁笅鏂囩淮鎶ゃ€佸巻鍙茬鐞嗐€佽嚜鍔ㄦ竻鐞?
- 鉁?**Web 鐣岄潰**: 鎻愪緵娴嬭瘯鐣岄潰鍜?RESTful API


## 馃洜锔?鎶€鏈爤

| 鎶€鏈?| 鐗堟湰 | 璇存槑 |
|------|------|------|
| Java | 17 | 寮€鍙戣瑷€ |
| Spring Boot | 3.2.0 | 搴旂敤妗嗘灦 |
| Spring AI | - | AI Agent 妗嗘灦 |
| DashScope | 2.17.0 | 闃块噷浜?AI 鏈嶅姟 |
| Milvus | 2.6.10 | 鍚戦噺鏁版嵁搴?|

## 馃摝 鏍稿績妯″潡

```
SuperBizAgent/
鈹溾攢鈹€ src/main/java/org/example/
鈹?  鈹溾攢鈹€ controller/
鈹?  鈹?  鈹斺攢鈹€ ChatController.java        # 缁熶竴鎺ュ彛鎺у埗鍣?猸?
鈹?  鈹溾攢鈹€ service/
鈹?  鈹?  鈹溾攢鈹€ ChatService.java           # 瀵硅瘽鏈嶅姟 猸?
鈹?  鈹?  鈹溾攢鈹€ AiOpsService.java          # AIOps 鏈嶅姟 猸?
鈹?  鈹?  鈹溾攢鈹€ RagService.java            # RAG 鏈嶅姟
鈹?  鈹?  鈹斺攢鈹€ Vector*.java               # 鍚戦噺鏈嶅姟
鈹?  鈹溾攢鈹€ agent/tool/                    # Agent 宸ュ叿闆?
鈹?  鈹?  鈹溾攢鈹€ DateTimeTools.java         # 鏃堕棿宸ュ叿
鈹?  鈹?  鈹溾攢鈹€ InternalDocsTools.java     # 鏂囨。妫€绱?
鈹?  鈹?  鈹溾攢鈹€ QueryMetricsTools.java     # 鍛婅鏌ヨ
鈹?  鈹?  鈹斺攢鈹€ QueryLogsTools.java        # 鏃ュ織鏌ヨ
鈹?  鈹斺攢鈹€ config/                        # 閰嶇疆绫?
鈹溾攢鈹€ src/main/resources/
鈹?  鈹溾攢鈹€ static/                        # Web 鐣岄潰
鈹?  鈹斺攢鈹€ application.yml                # 搴旂敤閰嶇疆
鈹斺攢鈹€ aiops-docs/                        # 杩愮淮鏂囨。搴?
```


## 馃摗 鏍稿績鎺ュ彛

### 1. 鏅鸿兘闂瓟鎺ュ彛

**娴佸紡瀵硅瘽锛堟帹鑽愶級**
```bash
POST /api/chat_stream
Content-Type: application/json

{
  "Id": "session-123",
  "Question": "浠€涔堟槸鍚戦噺鏁版嵁搴擄紵"
}
```
鏀寔 SSE 娴佸紡杈撳嚭銆佽嚜鍔ㄥ伐鍏疯皟鐢ㄣ€佸杞璇濄€?

**鏅€氬璇?*
```bash
POST /api/chat
Content-Type: application/json

{
  "Id": "session-123",
  "Question": "浠€涔堟槸鍚戦噺鏁版嵁搴擄紵"
}
```
涓€娆℃€ц繑鍥炲畬鏁寸粨鏋滐紝鏀寔宸ュ叿璋冪敤鍜屽杞璇濄€?

### 2. AIOps 鏅鸿兘杩愮淮鎺ュ彛

```bash
POST /api/ai_ops
```
鑷姩鎵ц鍛婅鍒嗘瀽娴佺▼锛岀敓鎴愯繍缁存姤鍛婏紙SSE 娴佸紡杈撳嚭锛夈€?

### 3. 浼氳瘽绠＄悊

- `POST /api/chat/clear` - 娓呯┖浼氳瘽鍘嗗彶
- `GET /api/chat/session/{sessionId}` - 鑾峰彇浼氳瘽淇℃伅

### 4. 鏂囦欢绠＄悊

- `POST /api/upload` - 涓婁紶鏂囦欢骞惰嚜鍔ㄥ悜閲忓寲
- `GET /milvus/health` - Milvus 鍋ュ悍妫€鏌?


## 鈿欙笍 鏍稿績閰嶇疆

### application.yml

```yaml
server:
  port: 9900

# Milvus 鍚戦噺鏁版嵁搴?
milvus:
  host: localhost
  port: 19530

# 闃块噷浜?DashScope
spring:
  ai:
    dashscope:
      api-key: "${DASHSCOPE_API_KEY}" // 鐜鍙橀噺

# RAG 閰嶇疆
rag:
  top-k: 3
  model: "qwen3-max"

# 鏂囨。鍒嗙墖
document:
  chunk:
    max-size: 800
    overlap: 100
```

### 鐜鍙橀噺

```bash
export DASHSCOPE_API_KEY=your-api-key
```


## 馃殌 蹇€熷紑濮?

### 1. 鐜鍑嗗

```bash
# 璁剧疆 API Key
export DASHSCOPE_API_KEY=your-api-key
```

### 2. 鍚姩搴旂敤

鏂规硶涓€锛?鎵嬪姩鍚姩
```bash
1.鍏堝惎鍔ㄥ悜閲忔暟鎹簱
docker compose up -d -f vector-database.yml

2.鍚姩鏈嶅姟
mvn clean install
mvn spring-boot:run
```

鏂规硶浜岋細涓€閿惎鍔?
```bash
make init  # 浼氳嚜鍔ㄥ惎鍔ㄥ悜閲忔暟鎹簱骞朵笂浼犺繍缁存枃妗ｅ埌鍚戦噺搴?
```


### 3. 浣跨敤绀轰緥

**Web 鐣岄潰**
```
http://localhost:9900
```

**鍛戒护琛?*
```bash
# 涓婁紶鏂囨。
curl -X POST http://localhost:9900/api/upload \
  -F "file=@document.txt"

# 鏅鸿兘闂瓟
curl -X POST http://localhost:9900/api/chat \
  -H "Content-Type: application/json" \
  -d '{"Id":"test","Question":"浠€涔堟槸鍚戦噺鏁版嵁搴擄紵"}'

# 鍋ュ悍妫€鏌?
curl http://localhost:9900/milvus/health
```


**鐗堟湰**: v1.0.0  
**浣滆€?*: chief  
**璁稿彲璇?*: MIT
