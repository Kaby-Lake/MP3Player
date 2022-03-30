# COMP3059MDP Coursework 1 - MP3Player Report



### Thrid-Party Libraries

- me.tatarka.bindingcollectionadapter2:bindingcollectionadapter
- me.tatarka.bindingcollectionadapter2:bindingcollectionadapter-recyclerview

Those two libiaries (under Apache License, can be found at https://github.com/evant/binding-collection-adapter) were used to support the `DataBinding` from `ViewModel` with `ObservableField` to `RecyclerView` with collection of views, therefore possible to apply `MVVM` pattern in this coursework.


### Attentions

It seems that there's a bug in the Emulator running API 29 (arm64-v8) which causes the app to continue display in the "Recent Applications" even if already swiped out (it happens not only to my application, but also the system-built ones). This anomaly does not happen in the real device running API 29.