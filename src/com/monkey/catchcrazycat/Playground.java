package com.monkey.catchcrazycat;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class Playground extends SurfaceView implements OnTouchListener {

	private static final int ROW = 10;
	private static final int COL = 10;
	private static final int BLOCK = 10;// 默认路障数
	private static final String TAG = "Playground";
	private int WINDTH = 25;
	Dot matrix[][];
	private Dot cat;
	int k = 1;

	public Playground(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		getHolder().addCallback(callback);
		matrix = new Dot[ROW][COL];

		for (int i = 0; i < ROW; i++) {
			for (int j = 0; j < COL; j++) {
				matrix[i][j] = new Dot(j, i);
			}
		}
		setOnTouchListener(this);
		initGame();

	}

	private Dot getDot(int x, int y) {
		return matrix[y][x];

	}

	// 判断是否在边界
	private boolean isAtEdge(Dot dot) {
		if (dot.getX() * dot.getY() == 0 || dot.getX() + 1 == COL || dot.getY() + 1 == ROW) {
			return true;
		}

		return false;
	}

	// 判断是否相邻
	private Dot getNrighbour(Dot one, int dir) {
		switch (dir) {
		case 1:
			return getDot(one.getX() - 1, one.getY());

		case 2:
			if (one.getY() % 2 == 0) {
				return getDot(one.getX() - 1, one.getY() - 1);
			} else {
				return getDot(one.getX(), one.getY() - 1);
			}

		case 3:
			if (one.getY() % 2 == 0) {
				return getDot(one.getX(), one.getY() - 1);
			} else {
				return getDot(one.getX() + 1, one.getY() - 1);
			}

		case 4:
			return getDot(one.getX() + 1, one.getY());

		case 5:
			if (one.getY() % 2 == 0) {
				return getDot(one.getX(), one.getY() + 1);
			} else {
				return getDot(one.getX() + 1, one.getY() + 1);
			}
		case 6:
			if (one.getY() % 2 == 0) {
				return getDot(one.getX() - 1, one.getY() + 1);
			} else {
				return getDot(one.getX(), one.getY() + 1);
			}

		}

		return null;
	}

	private int getDistance(Dot one, int dir) {
		int distance = 0;
		Dot origin = one, next;
		if (isAtEdge(origin)) {
			return 1;
		}
		while (true) {
			next = getNrighbour(origin, dir);
			if (next.getStatus() == Dot.STATUS_ON) {// 遇到路障
				return distance * -1;
			}
			if (isAtEdge(next)) {
				distance++;
				return distance;// 已经到了边缘
			}
			distance++;
			origin = next;
		}

	}

	// 猫移动的方法
	private void MoveTo(Dot one) {
		one.setStatus(Dot.STATUS_IN);
		getDot(cat.getX(), cat.getY()).setStatus(Dot.STATUS_OFF);
		;// 当前的位置复位
		cat.setXY(one.getX(), one.getY());
	}

	private void move() {
		if (isAtEdge(cat)) {
			// 游戏失败
			lose();
			return;
		}
		Vector<Dot> avaliable = new Vector<Dot>();
		Vector<Dot> positive = new Vector<Dot>();
		Map<Dot, Integer> al = new HashMap<Dot, Integer>();
		for (int i = 1; i < 7; i++) {
			Dot n = getNrighbour(cat, i);
			if (n.getStatus() == Dot.STATUS_OFF) {
				avaliable.add(n);
				al.put(n, i);
				if (getDistance(n, i) > 0) {// 直达边界的
					positive.add(n);
				}
			}

		}
		if (avaliable.size() == 0) {
			win();
		} else if (avaliable.size() == 1) {// 只有一个方向可以走
			MoveTo(avaliable.get(0));
		} /*else if (justinit) {
			int ran = (int) (Math.random() * 1000 % avaliable.size());
			MoveTo(avaliable.get(ran));

		} */else {// 不是第一次点击，有多条路可以走
			Dot best = null;
			if (positive.size() != 0) {
				int min = 999;
				for (int i = 0; i < positive.size(); i++) {
					int a = getDistance(positive.get(i), al.get(positive.get(i)));
					if (a < min) {
						min = a;
						best = positive.get(i);
					}
				}

			} else {
				// 所有方向都有路障
				int max = 0;
				for (int i = 0; i < avaliable.size(); i++) {
					int k = getDistance(avaliable.get(i), al.get(avaliable.get(i)));
					if (k < max) {
						max = k;
						best = avaliable.get(i);
					}
				}
			}
			MoveTo(best);
		}
	}

	private void win() {
		// TODO Auto-generated method stub
		Toast.makeText(getContext(), "You Win", Toast.LENGTH_SHORT).show();
	}

	private void lose() {

		Toast.makeText(getContext(), "Lose", Toast.LENGTH_SHORT).show();
	}

	private void redraw() {
		Canvas canvas = getHolder().lockCanvas();
		canvas.drawColor(Color.LTGRAY);
		Paint paint = new Paint();
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		for (int i = 0; i < ROW; i++) {
			int offset = 0;
			if (i % 2 != 0) {
				offset = WINDTH / 2;
			}
			for (int j = 0; j < COL; j++) {
				Dot one = getDot(j, i);
				switch (one.getStatus()) {
				case Dot.STATUS_OFF:
					paint.setColor(0xFFEEEEEE);
					break;
				case Dot.STATUS_ON:
					paint.setColor(0xFFFFAA00);

					break;
				case Dot.STATUS_IN:
					paint.setColor(0xFFFF0000);

					break;
				}
				canvas.drawOval(new RectF(one.getX() * WINDTH + offset, one.getY() * WINDTH,
						(one.getX() + 1) * WINDTH + offset, (one.getY() + 1) * WINDTH), paint);
			}
		}
		getHolder().unlockCanvasAndPost(canvas);

	}

	Callback callback = new Callback() {

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			redraw();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			// TODO Auto-generated method stub
			WINDTH = width / (COL + 1);
			redraw();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub

		}

	};

	private void initGame() {
		for (int i = 0; i < ROW; i++) {
			for (int j = 0; j < COL; j++) {
				matrix[i][j].setStatus(Dot.STATUS_OFF);
			}
		}

		cat = new Dot(4, 5);
		getDot(4, 5).setStatus(Dot.STATUS_IN);

		// 设置随机路障
		for (int i = 0; i < BLOCK;) {
			int x = (int) ((Math.random() * 1000) % COL);
			int y = (int) ((Math.random() * 1000) % ROW);
			if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
				getDot(x, y).setStatus(Dot.STATUS_ON);
				i++;
				Log.i(TAG, i + "");
			}
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_UP) {// 释放的一瞬间
			// Toast.makeText(getContext(), event.getX()+" "+event.getY(),
			// Toast.LENGTH_SHORT).show();
			int x, y;
			y = (int) (event.getY() / WINDTH);
			if (y % 2 == 0) {// 没有缩进
				x = (int) (event.getX() / WINDTH);
			} else {
				x = (int) ((event.getX() - WINDTH / 2) / WINDTH);
			}
			if (x + 1 > COL || y + 1 > ROW) {
				initGame();// 在区域之外

			} else if (getDot(x, y).getStatus() == Dot.STATUS_OFF) {
				getDot(x, y).setStatus(Dot.STATUS_ON);
				move();
			}
			redraw();
		}

		return true;
	}
}
