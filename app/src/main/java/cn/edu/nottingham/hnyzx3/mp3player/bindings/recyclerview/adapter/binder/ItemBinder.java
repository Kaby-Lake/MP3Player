package cn.edu.nottingham.hnyzx3.mp3player.bindings.recyclerview.adapter.binder;

public interface ItemBinder<T>
{
      int getLayoutRes(T model);
      int getBindingVariable(T model);
}
