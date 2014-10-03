package SandDrag;

public class VeMa {

	static float[][] testCircLineIntersect(float radius, float[][] line) {
		// make line relative to cent, since cent is supposed to be (0,0)
		float[] d = vectSub(line[1], line[0]);
		float da = norm(d);
		float capD = (line[0][0] * line[1][1]) - (line[1][0] * line[0][1]);
		float interX1 = (float) ((capD * d[1]) + ((sgn(d[1]) * d[0] * Math
				.sqrt((radius * radius) * (da * da) - (capD * capD)))))
				/ (da * da);
		float interY1 = (float) ((-capD * d[0]) + (Math.abs(d[1]) * Math
				.sqrt((radius * radius) * (da * da) - (capD * capD))))
				/ (da * da);
		float interX2 = (float) ((capD * d[1]) - ((sgn(d[1]) * d[0] * Math
				.sqrt((radius * radius) * (da * da) - (capD * capD)))))
				/ (da * da);
		float interY2 = (float) ((-capD * d[0]) - (Math.abs(d[1]) * Math
				.sqrt((radius * radius) * (da * da) - (capD * capD))))
				/ (da * da);
		return new float[][] { { interX1, interY1 }, { interX2, interY2 } };
	}

	static float[][] testCircLineIntersect(float[] cent, float radius,
			float[][] line) {
		line[0] = vectSub(line[0], cent);
		line[1] = vectSub(line[1], cent);
		// make line relative to cent, since cent is supposed to be (0,0)
		float[] d = vectSub(line[1], line[0]);
		float da = norm(d);
		float capD = (line[0][0] * line[1][1]) - (line[1][0] * line[0][1]);
		float interX1 = (float) ((capD * d[1]) + ((sgn(d[1]) * d[0] * Math
				.sqrt((radius * radius) * (da * da) - (capD * capD)))))
				/ (da * da);
		float interY1 = (float) ((-capD * d[0]) + (Math.abs(d[1]) * Math
				.sqrt((radius * radius) * (da * da) - (capD * capD))))
				/ (da * da);
		float interX2 = (float) ((capD * d[1]) - ((sgn(d[1]) * d[0] * Math
				.sqrt((radius * radius) * (da * da) - (capD * capD)))))
				/ (da * da);
		float interY2 = (float) ((-capD * d[0]) - (Math.abs(d[1]) * Math
				.sqrt((radius * radius) * (da * da) - (capD * capD))))
				/ (da * da);
		return new float[][] { { interX1, interY1 }, { interX2, interY2 } };
	}

	static int sgn(float n) {
		if (n < 0) {
			return -1;
		} else {
			return 1;
		}
	}

	static float distSegmenttoSegment(float[][] s1, float[][] s2) {
		double SMALL_NUM = 0.00000001;
		// Vector u = S1.P1 - S1.P0;
		float[] u = vectSub(s1[1], s1[0]);
		// Vector v = S2.P1 - S2.P0;
		float[] v = vectSub(s2[1], s2[0]);
		// Vector w = S1.P0 - S2.P0;
		float[] w = vectSub(s1[0], s2[0]);
		float a = dot(u, u); // always >= 0
		float b = dot(u, v);
		float c = dot(v, v); // always >= 0
		float d = dot(u, w);
		float e = dot(v, w);
		float D = a * c - b * b; // always >= 0
		float sc, sN, sD = D; // sc = sN / sD, default sD = D >= 0
		float tc, tN, tD = D; // tc = tN / tD, default tD = D >= 0

		// compute the line parameters of the two closest points
		if (D < SMALL_NUM) { // the lines are almost parallel
			sN = 0.0f; // force using point P0 on segment S1
			sD = 1.0f; // to prevent possible division by 0.0 later
			tN = e;
			tD = c;
		} else { // get the closest points on the infinite lines
			sN = (b * e - c * d);
			tN = (a * e - b * d);
			if (sN < 0.0f) { // sc < 0 => the s=0 edge is visible
				sN = 0.0f;
				tN = e;
				tD = c;
			} else if (sN > sD) { // sc > 1 => the s=1 edge is visible
				sN = sD;
				tN = e + b;
				tD = c;
			}
		}

		if (tN < 0.0f) { // tc < 0 => the t=0 edge is visible
			tN = 0.0f;
			// recompute sc for this edge
			if (-d < 0.0f)
				sN = 0.0f;
			else if (-d > a)
				sN = sD;
			else {
				sN = -d;
				sD = a;
			}
		} else if (tN > tD) { // tc > 1 => the t=1 edge is visible
			tN = tD;
			// recompute sc for this edge
			if ((-d + b) < 0.0f)
				sN = 0;
			else if ((-d + b) > a)
				sN = sD;
			else {
				sN = (-d + b);
				sD = a;
			}
		}
		// finally do the division to get sc and tc
		sc = (float) (Math.abs(sN) < SMALL_NUM ? 0.0 : sN / sD);
		tc = (float) (Math.abs(tN) < SMALL_NUM ? 0.0 : tN / tD);

		// get the difference of the two closest points
		// Vector dP = w + (sc * u) - (tc * v); // = S1(sc) - S2(tc)
		float[] dP = vectAdd(w,
				vectSub(vectMultScalar(sc, u), vectMultScalar(tc, v))); // =
																		// S1(sc)
																		// -
																		// S2(tc)

		// s1[0][0] + (s1[1] - s1[0]) * sc

		float[] s1loc = vectAdd(s1[0],
				vectMultScalar(sc, vectSub(s1[1], s1[0])));
		// System.out.println("loc1 (" + s1loc[0] + ", " + s1loc[1] + ")");

		float[] s2loc = vectAdd(s2[0],
				vectMultScalar(tc, vectSub(s2[1], s2[0])));

		return norm(dP); // return the closest distance
	}

	public static boolean isLeftT(float ax, float ay, float bx, float by,
			float cx, float cy) {
		// is from point a to b.
		return ((ax - bx) * (cy - by) - (ay - by) * (cx - bx)) > 0;
	}

	float dotShapeThea(float[] t, float[] w) {
		float dotTW = (t[0] * w[0]) + (t[1] * w[1]);
		float ta = norm(t);
		float wa = norm(w);
		float thea = (float) Math.acos(dotTW / (ta * wa));
		return thea;
	}

	static void sayVect(String name, float[] vect) {
		System.out.println(name + " (" + vect[0] + ", " + vect[1] + ")");
	}

	/**
	 * array manipulation
	 */

	static int[] appendIntAR(int[] st, int appendage) {
		int[] temp = new int[st.length + 1];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	static float[] appendFloatAR(float[] st, float appendage) {
		float[] temp = new float[st.length + 1];
		for (int a = 0; a < st.length; a++) {
			temp[a] = st[a];
		}
		temp[temp.length - 1] = appendage;
		return temp;
	}

	int[] shortenAR(int[] in, int numToRemove) {
		// This removed the [numToRemove] variable from an AR and compresses
		int[] temp = new int[in.length - 1];
		boolean reachedYet = false;
		for (int a = 0; a < in.length; a++) {
			System.out.println("a: " + a);
			if (a == numToRemove) {
				reachedYet = true;
				a++;
				System.out.println("newA: " + a);
			}
			if (a < in.length) {
				if (reachedYet) {
					temp[a - 1] = in[a];
				} else {
					temp[a] = in[a];
				}
			}
		}
		return temp;
	}

	// Vect manipulation methods

	static float dot(float[] a, float[] b) {
		float dp = (a[0] * b[0]) + (a[1] * b[1]);
		return dp;
	}

	static float norm(float[] v) {
		return (float) Math.sqrt(dot(v, v));
	}

	static float[] vectMultScalar(float scalar, float[] vect) {
		// this only works for 2d arrays but can make one that is less efficient
		// that works for anything.
		return new float[] { vect[0] * scalar, vect[1] * scalar };
	}

	static float[] vectDivScalar(float scalar, float[] vect) {
		return new float[] { vect[0] / scalar, vect[1] / scalar };
	}

	static float[] vectDivScalar(float scalar, int[] vect) {
		return new float[] { (float) (vect[0]) / scalar,
				(float) vect[1] / scalar };
	}

	static float[] vectAdd(float[] a, float[] b) {
		return new float[] { a[0] + b[0], a[1] + b[1] };
	}

	static float[] vectSub(float[] a, float[] b) {
		// a minus b.
		// b subtracted from a.
		return new float[] { a[0] - b[0], a[1] - b[1] };
	}

	static float[] getA1(float[] a, float[] b) {
		// sayVect("a", a);
		// sayVect("b", b);
		// project point a onto wall b.
		// |b|
		float ba = (float) Math.hypot(b[0], b[1]);
		float[] bhat = { b[0] / ba, b[1] / ba };
		float ascalar = ((a[0] * b[0]) + (a[1] * b[1])) / ba;
		float[] a1 = { (ascalar * bhat[0]), (ascalar * bhat[1]) };
		// sayVect("a1", a1);
		return a1;
	}

	static float[] returnLineIntersection(float p0_x, float p0_y, float p1_x,
			float p1_y, float p2_x, float p2_y, float p3_x, float p3_y) {
		// knows the points collide already.
		float s1_x, s1_y, s2_x, s2_y;
		s1_x = p1_x - p0_x;
		s1_y = p1_y - p0_y;
		s2_x = p3_x - p2_x;
		s2_y = p3_y - p2_y;

		float s, t;
		s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y))
				/ (-s2_x * s1_y + s1_x * s2_y);
		t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x))
				/ (-s2_x * s1_y + s1_x * s2_y);

		// if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
		float ix = p0_x + (t * s1_x);
		float iy = p0_y + (t * s1_y);
		return new float[] { ix, iy, s, t };
	}

	static float[] returnLineIntersection(float[][] line1, float[][] line2) {
		// knows the points collide already.
		float s1_x, s1_y, s2_x, s2_y;
		// s1_x = p1_x - p0_x;
		s1_x = line1[1][0] - line1[0][0];
		s1_y = line1[1][1] - line1[0][1];
		s2_x = line2[1][0] - line2[0][0];
		s2_y = line2[1][1] - line2[0][1];

		float s, t;
		s = (-s1_y * (line1[0][0] - line2[0][0]) + s1_x
				* (line1[0][1] - line2[0][1]))
				/ (-s2_x * s1_y + s1_x * s2_y);
		t = (s2_x * (line1[0][1] - line2[0][1]) - s2_y
				* (line1[0][0] - line2[0][0]))
				/ (-s2_x * s1_y + s1_x * s2_y);

		// if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
		float ix = line1[0][0] + (t * s1_x);
		float iy = line1[0][1] + (t * s1_y);
		return new float[] { ix, iy, s, t };
	}
}
