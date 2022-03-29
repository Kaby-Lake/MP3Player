需要的功能
- 从SD卡里选择音乐来播放
- 背景播放
- 换背景颜色

UI （可旋转）
- 主页
  - 文件选择（选择后播放）按钮
  - 暂停、继续播放、停止播放按钮
  - 进度条（可拖拽）
  - 换背景颜色
- 背景颜色选择页
  - 至少五种颜色可选
  - 返回到主页按钮
- Notification
  - 当音乐播放时显示通知（下拉通知界面里）

Service
- 不用AIDL
- 可以使用MediaPlayer class（无需手动创建新线程,也无需async load）

注意事项
- The application has an Activity that displays the contents of /sdcard/Music/
- The Service continues to play the track when the Activity is destroyed
- The Service is stopped when it is no longer needed
- The user should be able to start a music in another application and choose to open it with your Music Player app.（implicit Intents）
- Appropriate use of Widgets and ViewGroups for layouts that support devices of differing screen sizes and resolutions