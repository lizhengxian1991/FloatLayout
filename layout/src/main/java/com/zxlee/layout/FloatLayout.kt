package com.zxlee.layout

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * 根据按钮多少自动换行的ViewGroup
 */
class FloatLayout : ViewGroup {
    companion object {
        private val MARGIN = 5
    }

    private var maxWidth: Int = 0// 可使用的最大宽度

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet,
                defStyle: Int) : super(context, attrs, defStyle)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        var widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var containerHeight = 0// 容器的高度,也就是本布局的高度。初始化赋值为0.
        var maxHeightInRow = 0 //记录每一行里最高的View的高度
        var maxWidthInRow = 0 //记录最宽的一行的宽度
        var widthRow = 0 //记录每一行目前的宽度
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            /**
             * measure(int widthMeasureSpec,int
             * heightMeasureSpec)用于设置子组件显示模式.有三个值：<br></br>
             * MeasureSpec.AT_MOST 该组件可以设置自己的大小,但是最大不能超过其父组件的限定<br></br>
             * MeasureSpec.EXACTLY 无论该组件设置大小是多少,都只能按照父组件限制的大小来显示<br></br>
             * MeasureSpec.UNSPECIFIED 该组件不受父组件的限制,可以设置任意大小
             */
            view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            if(view.measuredWidth > maxWidth){
                throw RuntimeException("子View比父View还宽，出错啦！")
            }
            if (view.measuredWidth + widthRow > maxWidth) {
                //超宽了，换行
                containerHeight += maxHeightInRow
                maxWidthInRow = Math.max(widthRow, maxWidthInRow)
                maxHeightInRow = view.measuredHeight
                widthRow = view.measuredWidth
            } else {
                //不换行，更新累计宽度和最大高度
                maxHeightInRow = Math.max(maxHeightInRow, view.measuredHeight)
                widthRow += view.measuredWidth
            }
        }
        containerHeight += maxHeightInRow //最后一行的高度要加上
        maxWidthInRow = Math.max(widthRow, maxWidthInRow)
        setMeasuredDimension(if(widthMode == MeasureSpec.AT_MOST) maxWidthInRow else maxWidth, containerHeight)// onMeasure方法的关键代码,该句设置父容器的大小。
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount// 获取子组件数
        var row = 1// 子组件行数,初始化赋值为1
        var left = 0// 子组件的左边“坐标”
        var right = 0// 子组件的右边“坐标”
        var top = 0// 子组件的顶部“坐标”
        var bottom = 0// 子组件的底部“坐标”
        val p = paddingLeft// 在父组件中设置的padding属性的值,该值显然也会影响到子组件在屏幕的显示位置
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            val width = view.measuredWidth// 测量子组件的宽
            val height = view.measuredHeight// 测量子组件的高
            left = p + right// ---------------------------------------------------备注1
            right = left + width// -----------------------------------------------备注2
            top = p * row + height * (row - 1)// ---------------------------------备注3
            bottom = top + height// ----------------------------------------------备注4
            if (right > maxWidth) {
                row++
                left = 0//每次换行后要将子组件左边“坐标”与右边“坐标”重新初始化
                right = 0
                left = p + right
                right = left + width
                top = p * row + height * (row - 1)
                bottom = top + height
            }
            view.layout(left, top, right, bottom)// 最后按照计算出来的“坐标”将子组件放在父容器内
        }
    }

}
