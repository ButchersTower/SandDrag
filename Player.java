package SandDrag;

import java.awt.Color;
import java.util.ArrayList;

public class Player {

	// make a method that is like movePlayRoundPoint till he can exit.
	// take in moveSpeed left, tar, and uses playLoc.
	// if he exits the point it uses VeToWa to tar.

	float[] playLoc;
	float radius = 20;
	float moveSpeed = 6;

	float[][][] allWalls;

	public Player(float[] playLoc) {
		this.playLoc = playLoc;
	}

	void setWalls(float[][][] allWalls) {
		this.allWalls = allWalls;
	}

	void VeToWa(float moveSpeedLeft, float[] tarLoc, int lastWall,
			int[] killWall, boolean tempTar, float[] firstTar, boolean goToEdge) {
		// if goToEdge and doesnt scale. then edge curve.
		System.out.println("**VeToWa**");
		// System.out.println("lastWall: " + lastWall);
		for (int kw = 0; kw < killWall.length; kw++) {
			// System.out.println("killWall[" + kw + "]: " + killWall[kw]);
		}
		float[] movV = VeMa.vectSub(tarLoc, playLoc);
		float movVa = VeMa.norm(movV);
		boolean scaleDown = false;
		// System.out.println("goToEdge: " + goToEdge);
		if (movVa > moveSpeedLeft) {
			// System.out.println("ScaleDown");
			scaleDown = true;
			float multScalar = moveSpeedLeft / movVa;
			movV = VeMa.vectMultScalar(multScalar, movV);
		} else {
			// System.out.println("noScale");
		}
		float[][] playMoveSeg = { playLoc, VeMa.vectAdd(movV, playLoc) };
		float[] distBetween = new float[allWalls.length];
		// runs throught all walls and finds distBetween.
		for (int db = 0; db < distBetween.length; db++) {
			distBetween[db] = VeMa.distSegmenttoSegment(playMoveSeg,
					allWalls[db]);
			// System.out.println("DistB[" + db + "]: " + distBetween[db]);
		}
		ArrayList<float[]> deltaInfo = new ArrayList<float[]>();
		// all under radius find deltaa.
		for (int ld = 0; ld < distBetween.length; ld++) {
			if (!(lastWall == ld)) {
				for (int kw = 0; kw < killWall.length; kw++) {
					// System.out.println("kWall[" + kw + "]: " + killWall[kw]
					// + "|    ld: " + ld);
					if (killWall[kw] == ld) {
						// System.out.println("killThis");
						// return;
					}
				}
				if (distBetween[ld] < radius) {
					float[] interLoc = VeMa.returnLineIntersection(playMoveSeg,
							allWalls[ld]);
					float[] plaV = VeMa.vectSub(playLoc, interLoc);
					float[] point0V = VeMa.vectSub(allWalls[ld][0], interLoc);
					float point0Va = VeMa.norm(point0V);
					float[] point1V = VeMa.vectSub(allWalls[ld][1], interLoc);
					float point1Va = VeMa.norm(point1V);
					// pick the edge farther from intersect inorder to make sure
					// the vector used is never (0,0)
					float[] plaV1;
					if (point0Va < point1Va) {
						plaV1 = VeMa.getA1(plaV, point1V);
					} else {
						plaV1 = VeMa.getA1(plaV, point0V);
					}
					float[] plaV2 = VeMa.vectSub(plaV, plaV1);
					float plaV2a = VeMa.norm(plaV2);
					float multScalar = radius / plaV2a;
					plaV1 = VeMa.vectMultScalar(multScalar, plaV1);
					plaV2 = VeMa.vectMultScalar(multScalar, plaV2);

					float[] delta = VeMa.vectSub(
							VeMa.vectAdd(interLoc, VeMa.vectAdd(plaV1, plaV2)),
							playLoc);
					// Panel.drawCircle(Color.RED, playLoc[0] + delta[0],
					// playLoc[1] + delta[1], 6);
					float deltaa = VeMa.norm(delta);
					// System.out.println("deltaa[" + ld + "]:  " + deltaa);
					deltaInfo
							.add(new float[] { ld, deltaa, delta[0], delta[1] });
				}
			} else {
				System.out.println("skip LastWall");
			}
		}
		if (deltaInfo.size() == 0) {
			// no collisions just move.
			playLoc = VeMa.vectAdd(playLoc, movV);
			// System.out.println("glide");
			if (goToEdge) {
				if (!scaleDown) {
					// System.out.println("turnArr");
					System.out.println("moveSpeedLeft: " + moveSpeedLeft);
					turnAround(moveSpeedLeft, lastWall, firstTar);
					return;
				}
			}
		} else {
			// pick lowest deltaa and go there.
			// Prioritize equi-deltaa lines that are not on killList.
			/**
			 * test
			 */
			boolean curOnKill = false;
			boolean newOnKill = false;
			int lowestDi = 0;

			for (int di = 1; di < deltaInfo.size(); di++) {
				// checks if this deltaInfo is on killWall.
				for (int kl = 0; kl < killWall.length; kl++) {
					if (deltaInfo.get(di)[0] == killWall[kl]) {
						newOnKill = true;
					}
				}
				if (curOnKill) {
					if (deltaInfo.get(di)[1] <= deltaInfo.get(lowestDi)[1]) {
						lowestDi = di;
						curOnKill = newOnKill;
					}
				} else {
					if (deltaInfo.get(di)[1] < deltaInfo.get(lowestDi)[1]) {
						lowestDi = di;
						curOnKill = newOnKill;
					}
				}
			}

			/**
			 * FIND project wallDelt onto wall and find if scalar is greater
			 * than 1 or less than 0, if so then turn around sooner.
			 */

			// if the delta of the closest wallInfo projected to that wall has a
			// scalar greater than 1 or less than 0 then move snug against edge
			// and turn sooner.

			// if plaA2 is under 20 and colliding you know it is with edge.
			// what abound rounding errors.

			// project delta onto wall.
			float[] deltRelPlay = { deltaInfo.get(lowestDi)[2],
					deltaInfo.get(lowestDi)[3] };
			System.out.println("deltaInfo.get(lowestDi)[0]: "
					+ deltaInfo.get(lowestDi)[0]);
			int alWalNum = (int) deltaInfo.get(lowestDi)[0];
			// (play + deltaRel) - wall0
			float[] deltRelWal = VeMa.vectSub(
					playLoc, allWalls[alWalNum][0]);
			float[] wallsVe = VeMa.vectSub(allWalls[alWalNum][1],
					allWalls[alWalNum][0]);
			Panel.drawCircle(Color.BLUE,
					VeMa.vectAdd(deltRelWal, allWalls[alWalNum][0]), 3);
			float[] deltA1 = VeMa.getA1(deltRelWal, wallsVe);
			Panel.drawCircle(Color.RED,
					VeMa.vectAdd(deltA1, allWalls[alWalNum][0]), 3);
			float relWalXrat;
			if (wallsVe[0] == 0) {
				relWalXrat = deltA1[1] / wallsVe[1];
			} else {
				relWalXrat = deltA1[0] / wallsVe[0];
			}
			System.out.println("relWalXrat: " + relWalXrat);
			if (relWalXrat > 1 || relWalXrat < 0) {
				// turn around.
				System.out.println("LETS SEE");
				turnAround(moveSpeedLeft, alWalNum, firstTar);
				return;
			}

			// float[] walDelt = VeMa
			// .vectAdd(interLoc, VeMa.vectAdd(plaV1, plaV2));

			// float plaWalXrat = plaV1[0] / walV[0];
			// if (Float.isNaN(plaWalXrat)) {
			// plaWalXrat = plaV1[1] / walV[1];
			// }
			// // System.out.println("plaWalXrat: " + plaWalXrat);
			// if (plaWalXrat < 0 || plaWalXrat > 1) {
			// // System.out.println("under");
			// System.out.println("moveSpeedLeft: " + moveSpeedLeft);
			// turnAround(moveSpeedLeft, lowWall, firstTar);
			// return;
			// }

			// System.out.println("this lowest DI: " +
			// deltaInfo.get(lowestDi)[0]);
			for (int kw = 0; kw < killWall.length; kw++) {
				if (killWall[kw] == deltaInfo.get(lowestDi)[0]) {
					// System.out.println("!!KILL!!");
					return;
				}
			}

			float multScalar;
			System.out.println("deltaInfo.get(lowestDi)[1]: "
					+ deltaInfo.get(lowestDi)[1]);
			if (deltaInfo.get(lowestDi)[1] > moveSpeedLeft) {
				multScalar = moveSpeedLeft / deltaInfo.get(lowestDi)[1];
			} else {
				multScalar = 1;
			}

			// System.out.println("moveAgainst : wall "
			// + deltaInfo.get(lowestDi)[0]);
			playLoc = new float[] {
					playLoc[0] + (deltaInfo.get(lowestDi)[2] * multScalar),
					playLoc[1] + (deltaInfo.get(lowestDi)[3] * multScalar) };
			System.out.println("m1veSpeedLeft: " + moveSpeedLeft);
			moveSpeedLeft -= multScalar * deltaInfo.get(lowestDi)[1];
			System.out.println("m2veSpeedLeft: " + moveSpeedLeft);
			killWall = VeMa.appendIntAR(killWall,
					(int) deltaInfo.get(lowestDi)[0]);
			projectMoveVect(moveSpeedLeft, tarLoc,
					(int) deltaInfo.get(lowestDi)[0], killWall, tempTar,
					firstTar);
		}
	}

	void VeToWaOld2(float moveSpeedLeft, float[] tarLoc, int lastWall,
			int[] killWall, boolean tempTar, float[] firstTar, boolean goToEdge) {
		// if goToEdge and doesnt scale. then edge curve.
		System.out.println("**VeToWa**");
		// System.out.println("lastWall: " + lastWall);
		for (int kw = 0; kw < killWall.length; kw++) {
			// System.out.println("killWall[" + kw + "]: " + killWall[kw]);
		}
		float[] movV = VeMa.vectSub(tarLoc, playLoc);
		float movVa = VeMa.norm(movV);
		boolean scaleDown = false;
		// System.out.println("goToEdge: " + goToEdge);
		if (movVa > moveSpeedLeft) {
			// System.out.println("ScaleDown");
			scaleDown = true;
			float multScalar = moveSpeedLeft / movVa;
			movV = VeMa.vectMultScalar(multScalar, movV);
		} else {
			// System.out.println("noScale");
		}
		float[][] playMoveSeg = { playLoc, VeMa.vectAdd(movV, playLoc) };
		float[] distBetween = new float[allWalls.length];
		// runs throught all walls and finds distBetween.
		for (int db = 0; db < distBetween.length; db++) {
			distBetween[db] = VeMa.distSegmenttoSegment(playMoveSeg,
					allWalls[db]);
			// System.out.println("DistB[" + db + "]: " + distBetween[db]);
		}
		ArrayList<float[]> deltaInfo = new ArrayList<float[]>();
		// all under radius find deltaa.
		for (int ld = 0; ld < distBetween.length; ld++) {
			if (!(lastWall == ld)) {
				for (int kw = 0; kw < killWall.length; kw++) {
					// System.out.println("kWall[" + kw + "]: " + killWall[kw]
					// + "|    ld: " + ld);
					if (killWall[kw] == ld) {
						// System.out.println("killThis");
						// return;
					}
				}
				if (distBetween[ld] < radius) {
					float[] interLoc = VeMa.returnLineIntersection(playMoveSeg,
							allWalls[ld]);
					float[] plaV = VeMa.vectSub(playLoc, interLoc);
					float[] point0V = VeMa.vectSub(allWalls[ld][0], interLoc);
					float point0Va = VeMa.norm(point0V);
					float[] point1V = VeMa.vectSub(allWalls[ld][1], interLoc);
					float point1Va = VeMa.norm(point1V);
					// pick the edge farther from intersect inorder to make sure
					// the vector used is never (0,0)
					float[] plaV1;
					if (point0Va < point1Va) {
						plaV1 = VeMa.getA1(plaV, point1V);
					} else {
						plaV1 = VeMa.getA1(plaV, point0V);
					}
					float[] plaV2 = VeMa.vectSub(plaV, plaV1);
					float plaV2a = VeMa.norm(plaV2);
					float multScalar = radius / plaV2a;
					plaV1 = VeMa.vectMultScalar(multScalar, plaV1);
					plaV2 = VeMa.vectMultScalar(multScalar, plaV2);

					float[] delta = VeMa.vectSub(
							VeMa.vectAdd(interLoc, VeMa.vectAdd(plaV1, plaV2)),
							playLoc);
					// Panel.drawCircle(Color.RED, playLoc[0] + delta[0],
					// playLoc[1] + delta[1], 6);
					float deltaa = VeMa.norm(delta);
					// System.out.println("deltaa[" + ld + "]:  " + deltaa);
					deltaInfo
							.add(new float[] { ld, deltaa, delta[0], delta[1] });
				}
			} else {
				System.out.println("skip LastWall");
			}
		}
		if (deltaInfo.size() == 0) {
			// no collisions just move.
			playLoc = VeMa.vectAdd(playLoc, movV);
			// System.out.println("glide");
			if (goToEdge) {
				if (!scaleDown) {
					// System.out.println("turnArr");
					System.out.println("moveSpeedLeft: " + moveSpeedLeft);
					turnAround(moveSpeedLeft, lastWall, firstTar);
					return;
				}
			}
		} else {
			// pick lowest deltaa and go there.
			// Prioritize equi-deltaa lines that are not on killList.
			/**
			 * test
			 */
			boolean curOnKill = false;
			boolean newOnKill = false;
			int lowestDi = 0;

			for (int di = 1; di < deltaInfo.size(); di++) {
				// checks if this deltaInfo is on killWall.
				for (int kl = 0; kl < killWall.length; kl++) {
					if (deltaInfo.get(di)[0] == killWall[kl]) {
						newOnKill = true;
					}
				}
				if (curOnKill) {
					if (deltaInfo.get(di)[1] <= deltaInfo.get(lowestDi)[1]) {
						lowestDi = di;
						curOnKill = newOnKill;
					}
				} else {
					if (deltaInfo.get(di)[1] < deltaInfo.get(lowestDi)[1]) {
						lowestDi = di;
						curOnKill = newOnKill;
					}
				}
			}
			// System.out.println("this lowest DI: " +
			// deltaInfo.get(lowestDi)[0]);
			for (int kw = 0; kw < killWall.length; kw++) {
				if (killWall[kw] == deltaInfo.get(lowestDi)[0]) {
					// System.out.println("!!KILL!!");
					return;
				}
			}

			float multScalar;
			System.out.println("deltaInfo.get(lowestDi)[1]: "
					+ deltaInfo.get(lowestDi)[1]);
			if (deltaInfo.get(lowestDi)[1] > moveSpeedLeft) {
				multScalar = moveSpeedLeft / deltaInfo.get(lowestDi)[1];
			} else {
				multScalar = 1;
			}

			// System.out.println("moveAgainst : wall "
			// + deltaInfo.get(lowestDi)[0]);
			playLoc = new float[] {
					playLoc[0] + (deltaInfo.get(lowestDi)[2] * multScalar),
					playLoc[1] + (deltaInfo.get(lowestDi)[3] * multScalar) };
			System.out.println("m1veSpeedLeft: " + moveSpeedLeft);
			moveSpeedLeft -= multScalar * deltaInfo.get(lowestDi)[1];
			System.out.println("m2veSpeedLeft: " + moveSpeedLeft);
			killWall = VeMa.appendIntAR(killWall,
					(int) deltaInfo.get(lowestDi)[0]);
			projectMoveVect(moveSpeedLeft, tarLoc,
					(int) deltaInfo.get(lowestDi)[0], killWall, tempTar,
					firstTar);
		}
	}

	void projectMoveVect(float moveSpeedLeft, float[] tarLoc, int lowWall,
			int[] killWall, boolean tempTar, float[] firstTar) {
		System.out.println("moveSpeedLeft: " + moveSpeedLeft);
		System.out.println("**projectMoveVect**");
		// System.out.println("lowWall: " + lowWall);
		// pick the edge farther from target.
		float distToTar0 = VeMa.norm(VeMa.vectSub(allWalls[lowWall][0],
				firstTar));
		float distToTar1 = VeMa.norm(VeMa.vectSub(allWalls[lowWall][1],
				firstTar));
		float[] wallEnd;
		float[] wallStart;
		if (distToTar0 > distToTar1) {
			Panel.drawCircle(Color.GREEN, allWalls[lowWall][1][0],
					allWalls[lowWall][1][1], 6);
			// 0 is farther
			wallEnd = allWalls[lowWall][1];
			wallStart = allWalls[lowWall][0];
		} else {
			Panel.drawCircle(Color.ORANGE, allWalls[lowWall][0][0],
					allWalls[lowWall][0][1], 6);
			// 1 is farther
			wallEnd = allWalls[lowWall][0];
			wallStart = allWalls[lowWall][1];
		}
		float[] walV = VeMa.vectSub(wallEnd, wallStart);
		float walVa = VeMa.norm(walV);
		float[] plaV = VeMa.vectSub(playLoc, wallStart);
		float[] tarV = VeMa.vectSub(firstTar, wallStart);

		float[] plaV1 = VeMa.getA1(plaV, walV);
		float[] tarV1 = VeMa.getA1(tarV, walV);

		Panel.drawCircle(Color.ORANGE, wallStart[0] + tarV1[0], wallStart[1]
				+ tarV1[1], 6);

		// sayVect("tarV1", tarV1);
		// sayVect("walV", walV);
		float tarWalXrat = tarV1[0] / walV[0];
		if (Float.isNaN(tarWalXrat)) {
			tarWalXrat = tarV1[1] / walV[1];
		}
		float plaWalXrat = plaV1[0] / walV[0];
		if (Float.isNaN(plaWalXrat)) {
			plaWalXrat = plaV1[1] / walV[1];
		}
		// System.out.println("plaWalXrat: " + plaWalXrat);
		if (plaWalXrat < 0 || plaWalXrat > 1) {
			// System.out.println("under");
			System.out.println("moveSpeedLeft: " + moveSpeedLeft);
			turnAround(moveSpeedLeft, lowWall, firstTar);
			return;
		}

		// System.out.println("walVa: " + walVa);
		// System.out.println("tarWalXrat: " + tarWalXrat);
		boolean goToEdge = false;
		if (tarWalXrat > 1) {
			// tar past end of wall. scale it to the end.
			tarV1 = walV;
			goToEdge = true;
			// System.out.println("goToEdgeNext");
		}
		// if play1WalXrat is greater than 1 or less than 0.
		// curve player.`
		float[] nTarLoc = VeMa.vectAdd(playLoc, VeMa.vectSub(tarV1, plaV1));
		// System.out.println("continue");
		Panel.drawCircle(Color.BLUE, firstTar[0], firstTar[1], 6);
		Panel.drawLine(new Color(64, 188, 188), playLoc[0], playLoc[1],
				nTarLoc[0], nTarLoc[1]);
		VeToWa(moveSpeedLeft, nTarLoc, lowWall, killWall, tempTar, firstTar,
				goToEdge);
	}

	void turnAround(float moveSpeedLeft, int wallNum, float[] tarLoc) {
		System.out.println("moveSpeedLeft: " + moveSpeedLeft);
		Panel.drawCircle(Color.MAGENTA, tarLoc, 10);
		System.out.println("**turnAround**");
		float wall0DistToPlay = VeMa.norm(VeMa.vectSub(allWalls[wallNum][0],
				playLoc));
		float wall1DistToPlay = VeMa.norm(VeMa.vectSub(allWalls[wallNum][1],
				playLoc));
		// closer wall is center.
		boolean wall0Closer = false;
		float[][] touchWalls;
		int[] walls;
		float[] tarV;
		float[] center;
		if (wall0DistToPlay < wall1DistToPlay) {
			System.out.println("WALL ZERO IS CLOSER");
			// the lower wall is also close.
			wall0Closer = true;
			if (wallNum == 0) {
				// if wall num is 0 and point is 0
				// if wall num is .l-1 and point is 1
				touchWalls = new float[0][];
				walls = new int[0];
				// touchWalls[0] = VeMa.vectSub(allWalls[wallNum][1],
				// allWalls[wallNum][0]);
				// walls = new int[] { wallNum };
			} else {
				touchWalls = new float[2][];
				touchWalls[0] = VeMa.vectSub(allWalls[wallNum][1],
						allWalls[wallNum][0]);
				touchWalls[1] = VeMa.vectSub(allWalls[wallNum - 1][0],
						allWalls[wallNum][0]);
				walls = new int[] { wallNum, (wallNum - 1) };
				// System.out.println("wals: " + wallNum + ", " + (wallNum -
				// 1));
			}
			center = allWalls[wallNum][0];
			tarV = VeMa.vectSub(tarLoc, allWalls[wallNum][0]);
		} else {
			System.out.println("WALL WAHN IS CLOSER");
			if (wallNum == allWalls.length - 1) {
				// if wall num is .l-1 and point is 1
				touchWalls = new float[0][];
				walls = new int[0];
				// touchWalls[0] = VeMa.vectSub(allWalls[wallNum][0],
				// allWalls[wallNum][1]);
				// walls = new int[] { wallNum };
			} else {
				touchWalls = new float[2][];
				touchWalls[0] = VeMa.vectSub(allWalls[wallNum][0],
						allWalls[wallNum][1]);
				touchWalls[1] = VeMa.vectSub(allWalls[wallNum + 1][1],
						allWalls[wallNum][1]);
				// System.out.println("wals: " + wallNum + ", " + (wallNum +
				// 1));
				walls = new int[] { wallNum, (wallNum + 1) };
			}
			center = allWalls[wallNum][1];
			tarV = VeMa.vectSub(tarLoc, allWalls[wallNum][1]);
		}
		float[] plaV = VeMa.vectSub(playLoc, center);
		// float wallVa = VeMa.norm(VeMa.vectSub(allWalls[wallNum][0],
		// allWalls[wallNum][1]));

		float[][] touchA1 = new float[touchWalls.length][];
		float[] touchA1xrot = new float[touchWalls.length];
		for (int tw = 0; tw < touchWalls.length; tw++) {
			// tarProjected onto this wall, take that x and div by the walls x.
			// if > 0 then this is a preferred wall.
			touchA1[tw] = VeMa.getA1(tarV, touchWalls[tw]);
			// sayVect("touchA1[" + tw + "]", touchA1[tw]);
			// touchA1[tw] = VeMa.appendFloatAR(touchA1[tw], touchA1[tw][0]
			// / touchWalls[tw][0]);
			if (touchA1[tw][0] == 0) {
				touchA1xrot[tw] = touchA1[tw][1] / touchWalls[tw][1];
			} else {
				touchA1xrot[tw] = touchA1[tw][0] / touchWalls[tw][0];
			}
		}
		int[] preferredWalls = {};
		for (int tw = 0; tw < touchWalls.length; tw++) {
			// System.out.println("touchA1xrot[" + tw + "]: " +
			// touchA1xrot[tw]);
			if (touchA1xrot[tw] > 0) {
				// System.out.println("tw: " + tw);
				preferredWalls = VeMa.appendIntAR(preferredWalls, tw);
			}
		}
		// System.out.println("pW.l: " + preferredWalls.length);
		if (preferredWalls.length > 1) {
			// pick the wall with the lowest a2.
			float touchA2a = 1000;
			System.out.println("a2: " + touchA2a);
			int prefW = walls[preferredWalls[0]];
			for (int pw = 0; pw < preferredWalls.length; pw++) {
				// System.out.println("preferredWalls[la]: " +
				// preferredWalls[pw]);
				float[] tempA2 = VeMa
						.vectSub(tarV, touchA1[preferredWalls[pw]]);
				Panel.drawLine(Color.BLUE, VeMa.vectAdd(center,
						touchA1[preferredWalls[pw]]), VeMa.vectAdd(tempA2,
						VeMa.vectAdd(center, touchA1[preferredWalls[pw]])));
				float tempA2a = VeMa.norm(VeMa.vectSub(tarV,
						touchA1[preferredWalls[pw]]));
				// Panel.drawCircle(Color.BLUE,
				// VeMa.vectAdd(touchWalls[preferredWalls[pw]], center), 12);
				System.out.println("a2: " + tempA2a);
				if (tempA2a < touchA2a) {
					touchA2a = tempA2a;
					// System.out.println("pw: " + pw);
					// System.out.println("preferredWalls[pw]: "
					// + preferredWalls[pw]);
					// System.out.println("walls[preferredWalls[pw]]: "
					// + walls[preferredWalls[pw]]);
					prefW = preferredWalls[pw];
				}
			}
			System.out.println("prefW: " + prefW);
			// now i know preferred wall.
			// find the two points that are around center and for 90deg angles
			// with the pref wall.
			// how to pick the one that does not cause intersections with the
			// otherwall.

			prefWallHandle(prefW, touchWalls, center, tarV, plaV,
					moveSpeedLeft, touchA1[prefW], walls);
		} else {
			/**
			 * WORKING
			 */
			// there is only one wall attached to this so figure out which side
			// to go to.
			// not true.
			if (preferredWalls.length == 0) {
				System.out.println("oneWall");
				System.out.println("touWalls.l: " + touchWalls.length);

				playLoc = new float[] { 0, 0 };

				// move player against the edge of the wall.

				// pretend there is a circle around the point, find where movV
				// intersects with that circle and go there. If there are two
				// intersections go to the closer one.

				// find the point that player is near by.
				float[] edgePoint;
				if (wall0Closer) {
					edgePoint = allWalls[wallNum][0];
				} else {
					edgePoint = allWalls[wallNum][1];
				}

				// point of intersection of the imaginary circle from playLoc to
				// tarLoc. relative to edgePoint.
				float[][] interPoints = VeMa.testCircLineIntersect(20,
						new float[][] { plaV, tarV });
				float interP0dist = VeMa.norm(VeMa
						.vectSub(interPoints[0], plaV));
				float interP1dist = VeMa.norm(VeMa
						.vectSub(interPoints[1], plaV));
				System.out.println("interP0dist: " + interP0dist
						+ "    interP1dist: " + interP1dist);
				System.out.println("moveSpeedLeft: " + moveSpeedLeft);
				if (interP0dist < interP1dist) {
					playLoc = VeMa.vectAdd(interPoints[0], edgePoint);
					moveSpeedLeft -= interP0dist;
				} else {
					playLoc = VeMa.vectAdd(interPoints[1], edgePoint);
					moveSpeedLeft -= interP1dist;
				}
				// find the two points on that circle that are at the 90deg
				// angle between the center and target.

				float hyp = VeMa.norm(tarV);
				float theaPrim = (float) Math.acos(radius / hyp);
				float tarThea = pointToThea(tarV);
				float addThea = theaAdd(tarThea, theaPrim);
				float subThea = theaSub(tarThea, theaPrim);
				float[] pa1 = theaToPoint(addThea, radius);
				float[] pa2 = theaToPoint(subThea, radius);

				Panel.drawCircle(Color.BLACK, VeMa.vectAdd(edgePoint, pa1), 4);
				Panel.drawCircle(Color.BLUE, VeMa.vectAdd(edgePoint, pa2), 4);

				float[] otherEnd;
				if (wall0Closer) {
					otherEnd = VeMa.vectSub(allWalls[wallNum][1], center);
					// Panel.drawCircle(Color.GREEN, pa1, 10);
				} else {
					otherEnd = VeMa.vectSub(allWalls[wallNum][0], center);
				}
				float circPosa = VeMa.norm(VeMa.vectSub(pa1, otherEnd));
				float circNega = VeMa.norm(VeMa.vectSub(pa2, otherEnd));

				Panel.drawCircle(Color.ORANGE,
						VeMa.vectAdd(edgePoint, otherEnd), 12);

				float[] useCircP;
				if (circPosa > circNega) {
					// circPos farther so go to that. find the thea of that and
					// the thea of player and subtract. if newTheat * radius >
					// moveSpeed left tehn scale angle and move player. else go
					// there and sub movPlay less and make new vect to tar.
					useCircP = pa1;
				} else {
					useCircP = pa2;
				}
				Panel.drawCircle(Color.BLACK,
						VeMa.vectAdd(edgePoint, useCircP), 8);
				float circPthea = pointToThea(useCircP);
				float plaVthea = pointToThea(plaV);
				float deltaThea = theaSub(circPthea, plaVthea);
				boolean lowerThea = false;
				if (deltaThea < 0) {
					System.out.println("lowThea");
					lowerThea = true;
				}
				System.out.println("moveSpeedLeft: " + moveSpeedLeft);
				System.out.println("deltaThea: " + deltaThea);
				if (Math.abs(deltaThea) * radius < moveSpeedLeft) {
					System.out.println("move");
					playLoc = VeMa.vectAdd(edgePoint, useCircP);
					moveSpeedLeft -= Math.abs(deltaThea) * radius;
					VeToWa(moveSpeedLeft, tarLoc, wallNum, new int[0], false,
							tarLoc, false);
				} else {
					float moveThea = (moveSpeedLeft / radius);
					System.out.println("moveThea: " + moveThea);
					float newThea;
					if (lowerThea) {
						newThea = theaSub(plaVthea, moveThea);
					} else {
						newThea = theaAdd(plaVthea, moveThea);
					}
					float[] goToPoint = theaToPoint(newThea, radius);
					// sayVect("goToPoint", goToPoint);
					Panel.drawCircle(Color.BLUE,
							VeMa.vectAdd(goToPoint, edgePoint), 2);
					playLoc = VeMa.vectAdd(theaToPoint(newThea, radius),
							edgePoint);
					sayVect("playLoc", playLoc);
				}
			} else {
				System.out.println("elseOne");
				// there is one preffered wall. so
				prefWallHandle(preferredWalls[0], touchWalls, center, tarV,
						plaV, moveSpeedLeft, touchA1[preferredWalls[0]], walls);
			}

		}

		// project tar onto both walls. find the one with the lower scalar that
		// is greater than zero. and go there.
	}

	void movPlayRoundPoint(float moveSpeedLeft, float[] tarV, float[] plaV) {
		// i feel overwhelmed
	}

	void prefWallHandle(int prefW, float[][] touchWalls, float[] center,
			float[] tarV, float[] plaV, float moveSpeedLeft, float[] touchA1,
			int[] walls) {
		System.out.println("**prefWallHandle**");
		/**
		 * This makes circ go to.
		 */
		// get hat of pref wall. scale to radius. switch x and y and negate
		// one. that is new vect.
		float prefa = VeMa.norm(touchWalls[prefW]) / radius;
		float[] prefHat = VeMa.vectDivScalar(prefa, touchWalls[prefW]);
		float[] perpWall = { -prefHat[1], prefHat[0] };
		float[] pointAdd = perpWall;
		float[] pointSub = VeMa.vectSub(new float[2], perpWall);
		// sayVect("pointAdd", pointAdd);
		// sayVect("pointSub", pointSub);
		Panel.drawCircle(Color.GREEN, VeMa.vectAdd(pointAdd, center), 5);
		Panel.drawCircle(Color.BLUE, VeMa.vectAdd(pointSub, center), 5);
		float distAdd = VeMa.norm(VeMa.vectSub(tarV, pointAdd));
		float distSub = VeMa.norm(VeMa.vectSub(tarV, pointSub));
		// System.out.println("distA: " + distAdd + "   distS: " + distSub);
		float[] circGoTo;
		if (distAdd > distSub) {
			circGoTo = pointAdd;
		} else {
			circGoTo = pointSub;
		}
		Panel.drawCircle(Color.BLACK, VeMa.vectAdd(center, circGoTo), 3);
		// Panel.drawCircle(Color.BLACK, VeMa.vectSub(center, perpWall), 3);
		/**
		 * New circ go to DEGREES GREATER THAN 90
		 */
		// find if point(vect) is inside two vects. all start at origin.
		// there are two walls. take the first one, find if the other walls is
		// left or right.
		boolean secondIsLeft = false;
		if (isLeft(0, 0, touchWalls[0][0], touchWalls[0][1], touchWalls[1][0],
				touchWalls[1][1])) {
			secondIsLeft = true;
			System.out.println("SIL");
		}
		boolean playOutside = false;
		boolean pointAddOut = false;
		boolean pointSubOut = false;

		if (secondIsLeft) {
			// finds if play is outside.
			// if play is right of wall1 and left of wall2
			System.out.println("1: "
					+ isLeft(touchWalls[0][0], touchWalls[0][1], 0, 0, plaV[0],
							tarV[1])
					+ "    2: "
					+ isLeft(touchWalls[1][0], touchWalls[1][1], 0, 0, plaV[0],
							plaV[1]));
			if (isLeft(touchWalls[0][0], touchWalls[0][1], 0, 0, plaV[0],
					plaV[1])
					|| !isLeft(touchWalls[1][0], touchWalls[1][1], 0, 0,
							plaV[0], plaV[1])) {
				System.out.println("playOtuside1");
				playOutside = true;
			} else {
				System.out.println("inside1");
			}
			// find which point (90deg point) is outside.
			if (isLeft(touchWalls[0][0], touchWalls[0][1], 0, 0, pointAdd[0],
					pointAdd[1])
					|| !isLeft(touchWalls[1][0], touchWalls[1][1], 0, 0,
							pointAdd[0], pointAdd[1])) {
				System.out.println("PointAddOut");
				pointAddOut = true;
			} else {
				System.out.println("PointAddIn");
			}
			if (isLeft(touchWalls[0][0], touchWalls[0][1], 0, 0, pointSub[0],
					pointSub[1])
					|| !isLeft(touchWalls[1][0], touchWalls[1][1], 0, 0,
							pointSub[0], pointSub[1])) {
				System.out.println("PointSubOut");
				pointSubOut = true;
			} else {
				System.out.println("PointSubIn");
			}
		} else {
			if (!isLeft(touchWalls[0][0], touchWalls[0][1], 0, 0, plaV[0],
					plaV[1])
					|| isLeft(touchWalls[1][0], touchWalls[1][1], 0, 0,
							plaV[0], plaV[1])) {
				System.out.println("playOtuside2");
				playOutside = true;
			} else {
				System.out.println("inside2");
			}
			// find which point (90deg point) is outside.
			if (!isLeft(touchWalls[0][0], touchWalls[0][1], 0, 0, pointAdd[0],
					pointAdd[1])
					|| isLeft(touchWalls[1][0], touchWalls[1][1], 0, 0,
							pointAdd[0], pointAdd[1])) {
				System.out.println("PointAddOut");
				pointAddOut = true;
			} else {
				System.out.println("PointAddIn");
			}
			if (!isLeft(touchWalls[0][0], touchWalls[0][1], 0, 0, pointSub[0],
					pointSub[1])
					|| isLeft(touchWalls[1][0], touchWalls[1][1], 0, 0,
							pointSub[0], pointSub[1])) {
				System.out.println("PointSubOut");
				pointSubOut = true;
			} else {
				System.out.println("PointSubIn");
			}
		}
		if (pointSubOut) {
			circGoTo = pointSub;
		} else if (pointAddOut) {
			circGoTo = pointAdd;
		}

		// run throught the walls. if two find which

		// find angle of play.
		// find angle of leave
		// find dist between.
		// if its less than move speed less than go to.
		// if it is greater turn movespeed left to angle.
		// add angle to player and get (x,y) set player there.
		float playThea = pointToThea(plaV);
		System.out.println("SGT");
		float circGoThea = pointToThea(circGoTo);
		boolean decreaseThea = false;
		System.out.println("theaSub(playThea (" + playThea + "), circGoThea("
				+ circGoThea + ")): " + theaSub(playThea, circGoThea));
		if (theaSub(playThea, circGoThea) < 0) {
			decreaseThea = true;
		}
		float diffThea = theaSub(playThea, circGoThea);
		float goToDist = Math.abs(radius * diffThea);
		// System.out.println("goToDist: " + goToDist);
		if (goToDist < moveSpeedLeft) {
			playLoc = VeMa.vectAdd(circGoTo, center);
			moveSpeedLeft -= goToDist;
			// make player go to the tar projection. or just tar to test

			// project play, project tar subtract tar from play. make that
			// movV (tarLoc when added to playLoc). Ignore the prefWall.

			float[] newplaL1 = VeMa.getA1(circGoTo, touchWalls[prefW]);
			// Panel.drawCircle(Color.GREEN, VeMa.vectAdd(newplaL1, center),
			// 12);
			float[] newMovV = VeMa.vectSub(touchA1, newplaL1);
			float[] newMovVabs = VeMa.vectAdd(newMovV,
					VeMa.vectAdd(plaV, center));
			float[] firstTar = VeMa.vectAdd(tarV, center);
			Panel.drawLine(Color.RED, VeMa.vectAdd(plaV, center),
					VeMa.vectAdd(newMovV, VeMa.vectAdd(plaV, center)));
			/**
			 * CHECK is the final boolean supposed to be false?
			 */
			VeToWa(moveSpeedLeft, newMovVabs, walls[prefW], new int[0], true,
					firstTar, false);
		} else {
			// System.out.println("Dere");
			System.out.println("plaThea: " + playThea);
			float dunThea = moveSpeedLeft / radius;
			System.out.println("dunThea: " + dunThea);
			float newThea;
			if (decreaseThea) {
				System.out.println("decrease ++");
				newThea = playThea - dunThea;
				plaV = rotPoint(dunThea, plaV);
			} else {
				System.out.println("notDecre --");
				newThea = playThea + dunThea;
				plaV = rotPoint(-dunThea, plaV);
			}
			sayVect("plaV", plaV);
			playLoc = VeMa.vectAdd(plaV, center);
		}
	}

	void turnAroundOld(int wallNum, float[] tarLoc) {
		// rotates the player around an edge of a wall and checks and adjusts
		// for all other walls.
		System.out.println("***TurnAround*** " + wallNum);
		// find out which edge player is at.
		// make that edge (0,0) and the other (0, -|walV|)
		float wall0DistToPlay = VeMa.norm(VeMa.vectSub(allWalls[wallNum][0],
				playLoc));
		float wall1DistToPlay = VeMa.norm(VeMa.vectSub(allWalls[wallNum][1],
				playLoc));
		// closer wall is center.
		boolean wall0Closer = false;
		if (wall0DistToPlay < wall1DistToPlay) {
			wall0Closer = true;
		}
		float wallVa = VeMa.norm(VeMa.vectSub(allWalls[wallNum][0],
				allWalls[wallNum][1]));

		// play relative to closerWall.
		float[] newTar;
		float[] relPlay;
		float[] oldWall;
		if (wall0Closer) {
			oldWall = VeMa.vectSub(allWalls[wallNum][1], allWalls[wallNum][0]);
			relPlay = VeMa.vectSub(playLoc, allWalls[wallNum][0]);
			newTar = VeMa.vectSub(tarLoc, allWalls[wallNum][0]);
		} else {
			oldWall = VeMa.vectSub(allWalls[wallNum][0], allWalls[wallNum][1]);
			relPlay = VeMa.vectSub(playLoc, allWalls[wallNum][1]);
			newTar = VeMa.vectSub(tarLoc, allWalls[wallNum][1]);
		}

		float[] wallMarg = { 100, 200 };
		float[][] nWall = { { 0, 0 }, { wallVa, 0 } };

		Panel.drawLine(Color.BLUE, wallMarg, VeMa.vectAdd(wallMarg, nWall[1]));

		// thea to rotate by in order to get to the axis aligned rotation.
		float wallDisplace = dotShapeThea(nWall[1], oldWall);

		// if clock wise going to axis aligned: neg rot.
		// of the wall going straight up if oldwall is to the right then rot is
		// negative.
		if (isLeft(0, 0, nWall[1][0], nWall[1][1], oldWall[0], oldWall[1])) {
			// System.out.println("isLeft::");
			wallDisplace = -wallDisplace;
		} else {
			// System.out.println("isRight:");
		}

		// Panel.drawCircle(Color.BLUE, VeMa.vectAdd(wallMarg, relPlay), 3);
		Panel.drawCircle(Color.MAGENTA, VeMa.vectAdd(wallMarg, oldWall), 6);

		System.out
				.println("wallDisplaceD: " + (wallDisplace * (180 / Math.PI)));

		// float[] fakeRelPoint = { -14, 14 };
		// float[] fakePlay = rotPoint((29 * ((float) Math.PI / 180)),
		// fakeRelPoint);
		// sayVect("relPlay", relPlay);
		float[] fakePlay = rotPoint(-wallDisplace, relPlay);
		// sayVect("fakePlay", fakePlay);
		// Panel.drawCircle(Color.GREEN, VeMa.vectAdd(fakeRelPoint, wallMarg),
		// 3);
		Panel.drawCircle(Color.GREEN, VeMa.vectAdd(relPlay, wallMarg), 3);
		Panel.drawCircle(Color.BLACK, VeMa.vectAdd(fakePlay, wallMarg), 3);
		Panel.drawCircle(Color.BLUE, VeMa.vectAdd(newTar, wallMarg), 3);

		// how to figure which side of wall to go to.
		// need to find connecting walls.
		// walls that are close but not touching wouldnt fall under the
		// connecting search.
		// walls that have a distance under diameter should be walked away from.

		// find two points that allow for exit from edge.

		return;
	}

	void moveEnd(float[] tarV, float playDistLeft, boolean atWall1, float[] w1,
			float[] w2, float[] intersect) {
		float x = 0;
		float y = 0;
		float[] testC = new float[2];

		// Move around the edge.
		float[] wallUse;
		float[] wallOther;
		float[] center;
		if (atWall1) {
			wallUse = w1;
			wallOther = w2;
			center = VeMa.vectAdd(wallUse, intersect);
		} else {
			wallUse = w2;
			wallOther = w1;
			center = VeMa.vectAdd(wallUse, intersect);
		}
		float[] abosluteWU = VeMa.vectAdd(wallUse, intersect);
		float[] abosluteWO = VeMa.vectAdd(wallOther, intersect);

		float walla = VeMa.norm(VeMa.vectSub(wallOther, wallUse));
		// Make every point relative to wallUse (0,0)
		float[] nWallUse = { 0, 0 };
		float[] nWallOther = { 0, walla };
		float[] tar = VeMa.vectSub(tarV, abosluteWU);
		/**
		 * Draws Target
		 */
		// Panel.drawCircle(Color.WHITE, center[0] + tar[0], center[1] +
		// tar[1], 3);
		float tara = VeMa.norm(tar);

		float tarThea = (float) Math.atan(tar[1] / tar[0]);
		float wallThea = dotShapeThea(VeMa.vectSub(wallOther, wallUse),
				nWallOther);

		// If player is above wall. for rotation lock.
		boolean playAbove = false;
		if (y < intersect[1] + wallUse[1]) {
			playAbove = true;
		}

		// relative line
		Panel.drawLine(new Color(169, 33, 255), testC[0], testC[1], testC[0]
				+ nWallOther[0], testC[1] + nWallOther[1]);

		boolean negateLST = false;
		boolean tarRightWall = isLeft(intersect[0] + wallUse[0], intersect[1]
				+ wallUse[1], intersect[0] + wallOther[0], intersect[1]
				+ wallOther[1], tarV[0], tarV[1]);
		if (atWall1) {
			System.out.println("At Wall One (Bottom)");
			float tarTogether = (tarThea - wallThea);
			System.out.println("tarTogether: " + (tarTogether)
					* (180 / Math.PI) + "     tarTogether: " + tarTogether);
			System.out.print("tarT Pre: " + (tarTogether * (180 / Math.PI)));
			if (tarTogether < -Math.PI / 2) {
				tarTogether += Math.PI;
			}
			System.out.println("    tarT Post: "
					+ (tarTogether * (180 / Math.PI)));
			float[] rtar = { (float) (tara * Math.cos(tarTogether)),
					(float) (tara * Math.sin(tarTogether)) };

			float[] zTar;
			float rtara;
			float znx;
			float zny;
			float[] playz;
			float playThea;
			float lastShapeThea;

			if (tarRightWall) {

			} else {
				rtar[0] = -rtar[0];
				rtar[1] = -rtar[1];
			}

			Panel.drawCircle(Color.BLACK, testC[0] + rtar[0], testC[1]
					+ rtar[1], 6);

			// zTar = new float[] { -rtarx, -rtary };
			rtara = VeMa.norm(rtar);

			playz = new float[] { x - center[0], y - center[1] };
			float playza = VeMa.norm(playz);
			playThea = (float) Math.atan(playz[1] / playz[0]);
			System.out.println("playThea: " + (playThea * (180 / Math.PI))
					+ "\twallThea " + (wallThea * (180 / Math.PI)));
			// I negate the x
			float[] zPlay = { playza * (float) Math.cos(playThea - wallThea),
					playza * (float) Math.sin(playThea - wallThea) };
			if (isLeft(wallUse[0], wallUse[1], wallOther[0], wallOther[1], x
					- center[0], y - center[1])) {
				System.out.println("ORANGE ONE LEFT");
			} else {
				System.out.println("ORANGE ONE RIGHT");
			}

			if (playAbove) {
				if (playThea > 0) {
					zPlay[0] = -zPlay[0];
					zPlay[1] = -zPlay[1];
				}
			} else {
				if (playThea < 0) {
					zPlay[0] = -zPlay[0];
					zPlay[1] = -zPlay[1];
				}
			}

			Panel.drawCircle(Color.CYAN, testC[0] + zPlay[0], testC[1]
					+ zPlay[1], 5);
			System.out.println("zPlay (" + zPlay[0] + ", " + zPlay[1] + ")");

			// if tar qua is 1, 2 or 4 then it should have negative thea.

			System.out.println("rtar (" + rtar[0] + ", " + rtar[1] + ")");
			int tarQua = 0;
			if (rtar[1] < zPlay[1]) {
				System.out.println("above play");
				// quad 1 and 2
				if (rtar[0] < 0) {
					// quad 2 (or 3)
					tarQua = 2;
				} else {
					// quad 1
					tarQua = 1;
				}
			} else {
				System.out.println("below play");
				if (rtar[0] < 0) {
					// quad 3
					tarQua = 3;
				} else {
					// quad 4
					tarQua = 4;
				}
			}
			System.out.println("tarQua: " + tarQua);

			float rTarThea = (float) Math.atan(rtar[1] / rtar[0]);
			float rShapeThea = (float) Math.asin(radius / rtara);
			System.out.println("rTarThea: " + (rTarThea * (180 / Math.PI))
					+ "   rShapeThea: " + (rShapeThea * (180 / Math.PI)));
			float zA = (float) (rtara * Math.cos(rShapeThea));
			znx = (float) (zA * Math.cos(rTarThea + rShapeThea));
			zny = (float) (zA * Math.sin(rTarThea + rShapeThea));

			float[] znFromC = { znx + rtar[0], zny + rtar[1] };

			// if player is left left of wall.
			if (isLeft(wallUse[0], wallUse[1], wallOther[0], wallOther[1], x
					- center[0], y - center[1])) {
				System.out.println("aaa To The Right");

				if (tarQua == 4) {
					System.out.println("BLUIIE");
					// red, else blue
					// znFromC = new float[] { (znx + rtar[0]), zny + rtar[1] };
				} else {
					System.out.println("RAALD");
					// znFromC = new float[] { (-znxB + rtar[0]), -znyB +
					// rtar[1] };
					negateLST = true;
				}

				znx = (float) (zA * Math.cos(rTarThea - rShapeThea));
				zny = (float) (zA * Math.sin(rTarThea - rShapeThea));

				float znxB = (float) (zA * Math.cos(rTarThea + rShapeThea));
				float znyB = (float) (zA * Math.sin(rTarThea + rShapeThea));

				if (rtar[0] < 0) {
					znxB = -znxB;
					znyB = -znyB;
				} else {
					zny = -zny;
					znx = -znx;
				}

				// go (-x, -y)
				znFromC = new float[] { znx + rtar[0], zny + rtar[1] };
				// znFromC = new float[] { -znxB + rtar[0], -znyB + rtar[1] };
				Panel.drawCircle(Color.RED, testC[0] + znx + rtar[0], testC[1]
						+ zny + rtar[1], 6);
				System.out.println("black1");
				Panel.drawCircle(Color.BLACK, testC[0] + znFromC[0], testC[1]
						+ znFromC[1], 4);
				Panel.drawCircle(Color.BLUE, testC[0] - znxB + rtar[0],
						testC[1] - znyB + rtar[1], 6);
			} else {
				System.out.println("bbb to the left");

				System.out.println("rTarThea: " + (rTarThea * (180 / Math.PI))
						+ "    rShapeThea: " + (rShapeThea * (180 / Math.PI)));
				znx = (float) (zA * Math.cos(rTarThea - rShapeThea));
				zny = (float) (zA * Math.sin(rTarThea - rShapeThea));
				float znxB = (float) (zA * Math.cos(rTarThea + rShapeThea));
				float znyB = (float) (zA * Math.sin(rTarThea + rShapeThea));

				if (rtar[0] < 0) {
					znxB = -znxB;
					znyB = -znyB;
				} else {
					znx = -znx;
					zny = -zny;
				}

				// go (-x, -y)
				if (tarQua == 3) {
					// red, else blue
					System.out.println("RAALD");
					znFromC = new float[] { (znx + rtar[0]), zny + rtar[1] };
					negateLST = true;
				} else {
					System.out.println("BLUIIE");
					znFromC = new float[] { (-znxB + rtar[0]), -znyB + rtar[1] };
				}

				// minus rotated and inverted.
				Panel.drawCircle(Color.RED, testC[0] + znx + rtar[0], testC[1]
						+ zny + rtar[1], 6);
				System.out.println("black2");
				System.out.println("znFromc (" + znFromC[0] + ", " + znFromC[1]
						+ ")");
				Panel.drawCircle(Color.BLACK, testC[0] + znFromC[0], testC[1]
						+ znFromC[1], 4);
				// blyue is theatea tplugs thea.
				Panel.drawCircle(Color.BLUE, testC[0] - znxB + rtar[0],
						testC[1] - znyB + rtar[1], 5);
			}

			float znThea = (float) Math.atan(znFromC[1] / znFromC[0]);
			System.out.println("znThea: " + (znThea * (180 / Math.PI)));
			// Rotats anThea by wallRot
			// I negate (x and y).
			float[] zn1 = { (float) -(radius * Math.cos(znThea - wallThea)),
					(float) -(radius * Math.sin(znThea - wallThea)) };

			Panel.drawCircle(Color.RED, center[0] + zn1[0], center[1] + zn1[1],
					6);

			float[] playC = { x - center[0], y - center[1] };

			// lastShapeThea = getShapeThea(zn1, playC);
			lastShapeThea = dotShapeThea(zPlay, znFromC);
			System.out.println("LST get: " + (lastShapeThea * (180 / Math.PI)));

			float circumDist = lastShapeThea * radius;
			// System.out.println("circDist: " + circumDist);
			if (circumDist > playDistLeft) {
				lastShapeThea = playDistLeft / radius;
			}
			System.out.println("lastShapeThea: "
					+ (lastShapeThea * (180 / Math.PI)) + "   playThea: "
					+ (playThea * (180 / Math.PI)));
			// Find x and y of (lastShapeThea)
			float lSTx = (float) (radius * (Math.cos(lastShapeThea)));
			float lSTy = (float) (radius * (Math.sin(lastShapeThea)));

			if (negateLST) {
				System.out.println("negateLST");
				lastShapeThea = -lastShapeThea;
			}

			float zlSTx = (radius * (float) Math.cos(lastShapeThea + playThea));
			float zlSTy = (radius * (float) Math.sin(lastShapeThea + playThea));

			System.out.println("center (" + center[0] + ", " + center[1] + ")");
			if (playThea < 0) {
				if (playz[0] > 0) {
					System.out.println("add1");
					Panel.drawCircle(Color.CYAN, center[0] + zlSTx, center[1]
							+ zlSTy, 5);
					x = center[0] + zlSTx;
					y = center[1] + zlSTy;
				} else {
					System.out.println("add2");
					Panel.drawCircle(Color.CYAN, center[0] - zlSTx, center[1]
							- zlSTy, 5);
					x = center[0] - zlSTx;
					y = center[1] - zlSTy;
				}
			} else {
				Panel.drawCircle(Color.CYAN, center[0] + zlSTx, center[1]
						+ zlSTy, 5);
				System.out.println("add3");
				x = center[0] + zlSTx;
				y = center[1] + zlSTy;

			}
		} else {
			// Just fix right here
			// If target'x is lower than wall2's x then subtract x and y

			// temporary ntar.
			float tarTogether = (tarThea + wallThea);
			// System.out.println("tarTogether: " + (tarTogether)
			// * (180 / Math.PI) + "     tarTogether: " + tarTogether);
			System.out.print("tarT Pre: " + (tarTogether * (180 / Math.PI)));
			if (tarTogether > Math.PI / 2) {
				tarTogether -= Math.PI;
			}
			System.out.println("    tarT Post: "
					+ (tarTogether * (180 / Math.PI)));
			float[] rtar = { (float) (tara * Math.cos(tarTogether)),
					(float) (tara * Math.sin(tarTogether)) };

			float[] zTar;
			float rtara;
			float znx;
			float zny;
			float[] playz;
			float playThea;
			float lastShapeThea;

			if (tarRightWall) {

			} else {
				rtar[0] = -rtar[0];
				rtar[1] = -rtar[1];
			}

			Panel.drawCircle(Color.BLACK, testC[0] + rtar[0], testC[1]
					+ rtar[1], 6);

			// zTar = new float[] { -rtarx, -rtary };
			rtara = VeMa.norm(rtar);

			playz = new float[] { x - center[0], y - center[1] };
			float playza = VeMa.norm(playz);
			playThea = (float) Math.atan(playz[1] / playz[0]);
			System.out.println("playThea: " + (playThea * (180 / Math.PI))
					+ "\twallThea " + (wallThea * (180 / Math.PI)));
			// I negate the x
			float[] zPlay = { playza * (float) Math.cos(playThea + wallThea),
					playza * (float) Math.sin(playThea + wallThea) };
			if (isLeft(wallUse[0], wallUse[1], wallOther[0], wallOther[1], x
					- center[0], y - center[1])) {
				System.out.println("ORANGE ONE LEFT");
			} else {
				System.out.println("ORANGE ONE RIGHT");
			}

			if (playAbove) {
				if (playThea > 0) {
					zPlay[0] = -zPlay[0];
					zPlay[1] = -zPlay[1];
				}
			} else {
				if (playThea < 0) {
					zPlay[0] = -zPlay[0];
					zPlay[1] = -zPlay[1];
				}
			}

			Panel.drawCircle(Color.CYAN, testC[0] + zPlay[0], testC[1]
					+ zPlay[1], 5);
			System.out.println("zPlay (" + zPlay[0] + ", " + zPlay[1] + ")");

			// if tar qua is 1, 2 or 4 then it should have negative thea.

			int tarQua = 0;
			if (rtar[1] < zPlay[1]) {
				// quad 1 and 2
				if (rtar[0] < 0) {
					// quad 2 (or 3)
					tarQua = 2;
				} else {
					// quad 1
					tarQua = 1;
				}
			} else {
				if (rtar[0] < 0) {
					// quad 3
					tarQua = 3;
				} else {
					// quad 4
					tarQua = 4;
				}
			}
			System.out.println("tarQua: " + tarQua);

			float rTarThea = (float) Math.atan(rtar[1] / rtar[0]);
			float rShapeThea = (float) Math.asin(radius / rtara);
			System.out.println("rTarThea: " + (rTarThea * (180 / Math.PI))
					+ "   rShapeThea: " + (rShapeThea * (180 / Math.PI)));
			float zA = (float) (rtara * Math.cos(rShapeThea));
			znx = (float) (zA * Math.cos(rTarThea + rShapeThea));
			zny = (float) (zA * Math.sin(rTarThea + rShapeThea));
			// float zCircx = (float) (zTara * Math.acos(zTarThea
			// + zShapeThea));
			// float zCircy = (float) (zTara * Math.asin(zTarThea
			// + zShapeThea));
			// Mouse is past the wallEdge (after rotation)

			float[] znFromC = { znx + rtar[0], zny + rtar[1] };

			// if player is left left of wall.
			if (isLeft(wallUse[0], wallUse[1], wallOther[0], wallOther[1], x
					- center[0], y - center[1])) {
				System.out.println("aaa To The Right");

				if (tarQua == 4) {
					System.out.println("BLUIIE");
					// red, else blue
					// znFromC = new float[] { (znx + rtar[0]), zny + rtar[1] };
				} else {
					System.out.println("RAALD");
					// znFromC = new float[] { (-znxB + rtar[0]), -znyB +
					// rtar[1] };
					negateLST = true;
				}

				znx = (float) (zA * Math.cos(rTarThea - rShapeThea));
				zny = (float) (zA * Math.sin(rTarThea - rShapeThea));

				float znxB = (float) (zA * Math.cos(rTarThea + rShapeThea));
				float znyB = (float) (zA * Math.sin(rTarThea + rShapeThea));

				if (rtar[0] < 0) {
					znxB = -znxB;
					znyB = -znyB;
				} else {
					zny = -zny;
					znx = -znx;
				}

				// go (-x, -y)
				znFromC = new float[] { znx + rtar[0], zny + rtar[1] };
				// znFromC = new float[] { -znxB + rtar[0], -znyB + rtar[1] };
				Panel.drawCircle(Color.RED, testC[0] + znx + rtar[0], testC[1]
						+ zny + rtar[1], 6);
				System.out.println("black1");
				Panel.drawCircle(Color.BLACK, testC[0] + znFromC[0], testC[1]
						+ znFromC[1], 4);
				Panel.drawCircle(Color.BLUE, testC[0] - znxB + rtar[0],
						testC[1] - znyB + rtar[1], 6);
			} else {
				System.out.println("bbb to the left");

				System.out.println("rTarThea: " + (rTarThea * (180 / Math.PI))
						+ "    rShapeThea: " + (rShapeThea * (180 / Math.PI)));
				znx = (float) (zA * Math.cos(rTarThea - rShapeThea));
				zny = (float) (zA * Math.sin(rTarThea - rShapeThea));
				float znxB = (float) (zA * Math.cos(rTarThea + rShapeThea));
				float znyB = (float) (zA * Math.sin(rTarThea + rShapeThea));

				if (rtar[0] < 0) {
					znxB = -znxB;
					znyB = -znyB;
				} else {
					znx = -znx;
					zny = -zny;
				}

				// go (-x, -y)
				if (tarQua == 3) {
					// red, else blue
					System.out.println("RAALD");
					znFromC = new float[] { (znx + rtar[0]), zny + rtar[1] };
					negateLST = true;
				} else {
					System.out.println("BLUIIE");
					znFromC = new float[] { (-znxB + rtar[0]), -znyB + rtar[1] };
				}

				// minus rotated and inverted.
				Panel.drawCircle(Color.RED, testC[0] + znx + rtar[0], testC[1]
						+ zny + rtar[1], 6);
				System.out.println("black2");
				System.out.println("znFromc (" + znFromC[0] + ", " + znFromC[1]
						+ ")");
				Panel.drawCircle(Color.BLACK, testC[0] + znFromC[0], testC[1]
						+ znFromC[1], 4);
				// blyue is theatea tplugs thea.
				Panel.drawCircle(Color.BLUE, testC[0] - znxB + rtar[0],
						testC[1] - znyB + rtar[1], 5);
			}

			float znThea = (float) Math.atan(znFromC[1] / znFromC[0]);
			System.out.println("znThea: " + (znThea * (180 / Math.PI)));
			// Rotats anThea by wallRot
			// I negate (x and y).
			float[] zn1 = { (float) -(radius * Math.cos(znThea - wallThea)),
					(float) -(radius * Math.sin(znThea - wallThea)) };

			Panel.drawCircle(Color.RED, center[0] + zn1[0], center[1] + zn1[1],
					6);

			float[] playC = { x - center[0], y - center[1] };

			// lastShapeThea = getShapeThea(zn1, playC);
			lastShapeThea = dotShapeThea(zPlay, znFromC);
			System.out.println("LST get: " + (lastShapeThea * (180 / Math.PI)));

			float circumDist = lastShapeThea * radius;
			// System.out.println("circDist: " + circumDist);
			if (circumDist > playDistLeft) {
				lastShapeThea = playDistLeft / radius;
			}
			System.out.println("lastShapeThea: "
					+ (lastShapeThea * (180 / Math.PI)) + "   playThea: "
					+ (playThea * (180 / Math.PI)));
			// Find x and y of (lastShapeThea)
			float lSTx = (float) (radius * (Math.cos(lastShapeThea)));
			float lSTy = (float) (radius * (Math.sin(lastShapeThea)));

			if (negateLST) {
				System.out.println("negateLST");
				lastShapeThea = -lastShapeThea;
			}

			float zlSTx = (radius * (float) Math.cos(lastShapeThea + playThea));
			float zlSTy = (radius * (float) Math.sin(lastShapeThea + playThea));

			if (playThea < 0) {
				if (playz[0] > 0) {
					System.out.println("add4a");
					Panel.drawCircle(Color.CYAN, center[0] + zlSTx, center[1]
							+ zlSTy, 5);
					x = center[0] + zlSTx;
					y = center[1] + zlSTy;
				} else {
					System.out.println("add5a");
					Panel.drawCircle(Color.CYAN, center[0] - zlSTx, center[1]
							- zlSTy, 5);
					x = center[0] - zlSTx;
					y = center[1] - zlSTy;
				}
			} else {
				Panel.drawCircle(Color.CYAN, center[0] - zlSTx, center[1]
						- zlSTy, 5);
				Panel.drawCircle(Color.RED, center[0] + zlSTx, center[1]
						+ zlSTy, 12);
				Panel.drawCircle(Color.CYAN, center[0] + zlSTx, center[1]
						+ zlSTy, 10);
				System.out.println("add6a");
				System.out.println("drawLoc (" + (center[0] + zlSTx) + ", "
						+ (center[1] + zlSTy) + ")");
				x = center[0] - zlSTx;
				y = center[1] - zlSTy;
				// x = center[0] + zlSTx;
				// y = center[1] + zlSTy;
			}
		}
	}

	boolean isLeft(float ax, float ay, float bx, float by, float cx, float cy) {
		// is FROM a TO b.
		// (10,10) to (30,20) point (30,10) isLeft = true;
		return ((ax - bx) * (cy - by) - (ay - by) * (cx - bx)) > 0;
	}

	float dotShapeThea(float[] t, float[] w) {
		float dotTW = (t[0] * w[0]) + (t[1] * w[1]);
		float ta = (float) Math.hypot(t[0], t[1]);
		float wa = (float) Math.hypot(w[0], w[1]);
		float thea = (float) Math.acos(dotTW / (ta * wa));
		return thea;
	}

	float pointToThea(float[] point) {
		float pointThea = (float) Math.atan(point[1] / point[0]);
		// sayVect("point", point);
		// System.out.println("pointFirst: " + pointThea + " ("
		// + (pointThea * (180 / Math.PI) + ")"));
		if (point[1] > 0 && pointThea < 0) {
			System.out.println("change1");
			pointThea = (float) Math.PI + pointThea;
		} else if (point[1] < 0 && pointThea > 0) {
			// y is less than zero and thea is greater than zero.
			System.out.println("change2");
			pointThea = -(float) Math.PI + pointThea;
		}
		// System.out.println("pointMid: " + pointThea + " ("
		// + (pointThea * (180 / Math.PI) + ")"));
		if (pointThea == 0 && point[0] < 0) {
			System.out.println("zero to 360.");
			pointThea = (float) Math.PI;
		}
		return pointThea;
	}

	float[] theaToPoint(float thea, float radius) {
		return new float[] { (float) Math.cos(thea) * radius,
				(float) Math.sin(thea) * radius };
	}

	float[] rotPoint(float thea, float[] point) {
		System.out.println("**rotPoint**");
		System.out.println("inThea: " + thea + " ("
				+ ((thea) * (180 / Math.PI)));
		float pointa = VeMa.norm(point);
		boolean bothNeg = false;
		if (point[0] < 0 && point[1] < 0) {
			// bothNeg = true;
		}
		// converts point to thea.
		float pointThea = pointToThea(point);
		// sayVect("point", point);
		System.out.println("pointThea: " + pointThea);

		// adds theatas.
		float newThea = pointThea + thea;
		// System.out.println("old THEA: " + newThea + " ("
		// + (newThea * (180 / Math.PI)) + ")");
		if (newThea > Math.PI) {
			newThea = newThea - (float) Math.PI * 2;
		} else if (newThea < -Math.PI) {
			newThea = (float) (2 * Math.PI) + newThea;
		}

		// thea to point.
		// newPoint is hypotnuse.
		float[] newPoint = new float[2];
		// float nx = (float) Math.cos(newThea) * pointa;
		// float ny = (float) Math.tan(newThea) * nx;
		// System.out.println("sin: " + Math.sin(newThea) + "    pointa: "
		// + pointa);
		// System.out.println("tan: " + Math.tan(newThea));
		// System.out.println("NEW THEA: " + newThea + " ("
		// + (newThea * (180 / Math.PI)) + ")");
		/**
		 * CHEATE. radius should be pointa, but in order to make the program
		 * lookbetter it is set to radius. Fix the code and make it so pinta is
		 * equal to radius anyway.
		 */
		System.out.println("newThea: " + newThea + "(" + newThea + ")");
		float ny = (float) Math.sin(newThea) * pointa;
		float nx = 1 / ((float) Math.tan(newThea) / ny);
		if (newThea > Math.PI / 2) {
			// System.out.println("bug ++");
		} else if (newThea < -Math.PI / 2) {
			// System.out.println("bug --");
		}
		if (Float.isNaN(nx)) {
			nx = pointa;
		}
		// System.out.println("bothNeg: " + bothNeg);
		if (bothNeg) {
			nx = -nx;
			ny = -ny;
		}
		// System.out.println("afterRot (" + nx + ", " + ny + ")");

		return new float[] { nx, ny };
	}

	float[] rotPointOld1(float thea, float[] point) {
		System.out.println("**rotPoint**");
		System.out.println("inThea: " + thea + " ("
				+ ((thea) * (180 / Math.PI)));
		float pointa = VeMa.norm(point);
		boolean bothNeg = false;
		if (point[0] < 0 && point[1] < 0) {
			// bothNeg = true;
		}
		// rotate the float around the center by thea.
		// thea is between -180, and posative 180.
		// if thea is >90 or <-90 it is special.

		// first get thea of point.
		// thea = Tan(y/x)

		// converts point to thea.
		float pointThea = (float) Math.atan(point[1] / point[0]);
		// sayVect("point", point);
		// System.out.println("pointFirst: " + pointThea + " ("
		// + (pointThea * (180 / Math.PI) + ")"));
		if (point[1] > 0 && pointThea < 0) {
			// System.out.println("change1");
			pointThea = (float) Math.PI + pointThea;
		} else if (pointThea > 0) {
			// y is less than zero and thea is greater than zero.
			pointThea = -(float) Math.PI + pointThea;
			// System.out.println("change2");
		}
		if (pointThea == -0) {
			pointThea = (float) Math.PI;
		}
		System.out.println("pointThea: " + pointThea + " ("
				+ (pointThea * (180 / Math.PI) + ")"));

		// adds theatas.
		float newThea = pointThea + thea;
		// System.out.println("old THEA: " + newThea + " ("
		// + (newThea * (180 / Math.PI)) + ")");
		if (newThea > Math.PI) {
			newThea = newThea - (float) Math.PI * 2;
		} else if (newThea < -Math.PI) {
			newThea = (float) (2 * Math.PI) + newThea;
		}

		// thea to point.
		// newPoint is hypotnuse.
		float[] newPoint = new float[2];
		// float nx = (float) Math.cos(newThea) * pointa;
		// float ny = (float) Math.tan(newThea) * nx;
		// System.out.println("sin: " + Math.sin(newThea) + "    pointa: "
		// + pointa);
		// System.out.println("tan: " + Math.tan(newThea));
		// System.out.println("NEW THEA: " + newThea + " ("
		// + (newThea * (180 / Math.PI)) + ")");
		/**
		 * CHEATE. radius should be pointa, but in order to make the program
		 * lookbetter it is set to radius. Fix the code and make it so pinta is
		 * equal to radius anyway.
		 */
		System.out.println("newThea: " + newThea + "(" + newThea + ")");
		float ny = (float) Math.sin(newThea) * pointa;
		float nx = 1 / ((float) Math.tan(newThea) / ny);
		if (newThea > Math.PI / 2) {
			// System.out.println("bug ++");
		} else if (newThea < -Math.PI / 2) {
			// System.out.println("bug --");
		}

		if (Float.isNaN(nx)) {
			nx = pointa;
		}

		// System.out.println("bothNeg: " + bothNeg);
		if (bothNeg) {
			nx = -nx;
			ny = -ny;
		}
		// System.out.println("afterRot (" + nx + ", " + ny + ")");

		// radea = |point|
		// thea = sro
		// circ = 2 PI r

		return new float[] { nx, ny };
	}

	float theaAdd(float thea1, float thea2) {
		// adds two theas between -180 and 180 (in radians) and returned a thea
		// between -180 and 180
		float tempThea = thea1 + thea2;
		if (tempThea > Math.PI) {
			tempThea = -(float) Math.PI * 2 + tempThea;
		} else if (tempThea < -Math.PI) {
			tempThea = (float) Math.PI * 2 + tempThea;
		}
		return tempThea;
	}

	float theaSub(float thea1, float thea2) {
		// System.out.println("thea1: " + thea1 + "   thea2: " + thea2);
		// adds two theas between -180 and 180 (in radians) and returned a thea
		// between -180 and 180
		float tempThea = thea1 - thea2;
		if (tempThea > Math.PI) {
			tempThea = -(float) Math.PI * 2 + tempThea;
		} else if (tempThea < -Math.PI) {
			tempThea = (float) Math.PI * 2 + tempThea;
		}
		// System.out.println("tempThea: " + tempThea);
		return tempThea;
	}

	// vector to wall.
	void VeToWaOld1(float moveSpeedLeft, float[] tarLoc, int lastWall,
			int[] killWall, boolean tempTar, float[] firstTar, boolean goToEdge) {
		// if goToEdge and doesnt scale. then edge curve.
		System.out.println("*****");
		System.out.println("lastWall: " + lastWall);
		for (int kw = 0; kw < killWall.length; kw++) {
			System.out.println("killWall[" + kw + "]: " + killWall[kw]);
		}
		float[] movV = VeMa.vectSub(tarLoc, playLoc);
		float movVa = VeMa.norm(movV);
		if (movVa > moveSpeedLeft) {
			float multScalar = moveSpeedLeft / movVa;
			movV = VeMa.vectMultScalar(multScalar, movV);
		}
		float[][] playMoveSeg = { playLoc, VeMa.vectAdd(movV, playLoc) };
		float[] distBetween = new float[allWalls.length];
		// runs throught all walls and finds distBetween.
		for (int db = 0; db < distBetween.length; db++) {
			distBetween[db] = VeMa.distSegmenttoSegment(playMoveSeg,
					allWalls[db]);
		}
		ArrayList<float[]> deltaInfo = new ArrayList<float[]>();
		// all under radius find deltaa.
		for (int ld = 0; ld < distBetween.length; ld++) {
			if (!(lastWall == ld)) {
				for (int kw = 0; kw < killWall.length; kw++) {
					// System.out.println("kWall[" + kw + "]: " + killWall[kw]
					// + "|    ld: " + ld);
					if (killWall[kw] == ld) {
						// System.out.println("killThis");
						// return;
					}
				}
				if (distBetween[ld] < radius) {
					float[] interLoc = VeMa.returnLineIntersection(playMoveSeg,
							allWalls[ld]);
					float[] plaV = VeMa.vectSub(playLoc, interLoc);
					float[] point0V = VeMa.vectSub(allWalls[ld][0], interLoc);
					float point0Va = VeMa.norm(point0V);
					float[] point1V = VeMa.vectSub(allWalls[ld][1], interLoc);
					float point1Va = VeMa.norm(point1V);
					// pick the edge farther from intersect inorder to make sure
					// the vector used is never (0,0)
					float[] plaV1;
					if (point0Va < point1Va) {
						plaV1 = VeMa.getA1(plaV, point1V);
					} else {
						plaV1 = VeMa.getA1(plaV, point0V);
					}
					// sayVect("plaV1", plaV1);
					float[] plaV2 = VeMa.vectSub(plaV, plaV1);
					float plaV2a = VeMa.norm(plaV2);
					// scale plaV1 so that plaV2 is 10.
					// System.out.println("plaV2a: " + plaV2a);
					float multScalar = radius / plaV2a;
					plaV1 = VeMa.vectMultScalar(multScalar, plaV1);
					plaV2 = VeMa.vectMultScalar(multScalar, plaV2);

					float[] delta = VeMa.vectSub(
							VeMa.vectAdd(interLoc, VeMa.vectAdd(plaV1, plaV2)),
							playLoc);
					// System.out.println("delta[" + ld + "] (" + delta[0] +
					// ", " + delta[1]);
					Panel.drawCircle(Color.RED, playLoc[0] + delta[0],
							playLoc[1] + delta[1], 6);
					float deltaa = VeMa.norm(delta);
					System.out.println("deltaa[" + ld + "]:  " + deltaa);
					// sayVect("interLoc", interLoc);
					deltaInfo
							.add(new float[] { ld, deltaa, delta[0], delta[1] });
				}
			} else {
				System.out.println("skip");
			}
		}
		if (deltaInfo.size() == 0) {
			// no collisions just move.
			playLoc = VeMa.vectAdd(playLoc, movV);
			System.out.println("glide");
		} else {
			// pick lowest deltaa and go there.
			// Prioritize equi-deltaa lines that are not on killList.
			/**
			 * test
			 */
			boolean curOnKill = false;
			boolean newOnKill = false;
			int lowestDi = 0;

			for (int di = 1; di < deltaInfo.size(); di++) {
				// checks if this deltaInfo is on killWall.
				for (int kl = 0; kl < killWall.length; kl++) {
					if (deltaInfo.get(di)[0] == killWall[kl]) {
						newOnKill = true;
					}
				}
				if (curOnKill) {
					if (deltaInfo.get(di)[1] <= deltaInfo.get(lowestDi)[1]) {
						lowestDi = di;
						curOnKill = newOnKill;
					}
				} else {
					if (deltaInfo.get(di)[1] < deltaInfo.get(lowestDi)[1]) {
						lowestDi = di;
						curOnKill = newOnKill;
					}
				}
			}
			/**
			 * end
			 */
			// int lowestDi = 0;
			// for (int di = 1; di < deltaInfo.size(); di++) {
			// if (deltaInfo.get(di)[1] < deltaInfo.get(lowestDi)[1]) {
			// lowestDi = di;
			// }
			// }
			System.out.println("this lowest DI: " + deltaInfo.get(lowestDi)[0]);
			// run throught kill wall. if lowest deltaa wall is on that then
			// kill.
			// System.out.println("lastWall: " + lastWall);
			for (int kw = 0; kw < killWall.length; kw++) {
				// System.out.println("killWall[" + kw + "]: " + killWall[kw]);
				// System.out.println("ThisWall: " +
				// deltaInfo.get(lowestDi)[0]);
				if (killWall[kw] == deltaInfo.get(lowestDi)[0]) {
					System.out.println("!!KILL!!");
					return;
				}
			}

			float multScalar;
			if (deltaInfo.get(lowestDi)[1] > moveSpeedLeft) {
				multScalar = moveSpeedLeft / deltaInfo.get(lowestDi)[1];
			} else {
				multScalar = 1;
			}

			System.out.println("moveAgainst : wall "
					+ deltaInfo.get(lowestDi)[0]);
			playLoc = new float[] {
					playLoc[0] + (deltaInfo.get(lowestDi)[2] * multScalar),
					playLoc[1] + (deltaInfo.get(lowestDi)[3] * multScalar) };
			moveSpeedLeft -= multScalar * deltaInfo.get(lowestDi)[1];
			killWall = VeMa.appendIntAR(killWall,
					(int) deltaInfo.get(lowestDi)[0]);
			projectMoveVect(moveSpeedLeft, tarLoc,
					(int) deltaInfo.get(lowestDi)[0], killWall, tempTar,
					firstTar);
		}

	}

	void projectMoveVectOld1(float moveSpeedLeft, float[] tarLoc, int lowWall,
			int[] killWall, boolean tempTar, float[] firstTar) {
		System.out.println("lowWall: " + lowWall);
		// get play1
		// get tar1
		// if tar1 off wall set to edge
		// sub play1 from tar1
		// add that to play.
		// that is target.

		// pick the edge farther from target.
		float distToTar0 = VeMa.norm(VeMa.vectSub(allWalls[lowWall][0],
				firstTar));
		float distToTar1 = VeMa.norm(VeMa.vectSub(allWalls[lowWall][1],
				firstTar));
		float[] wallEnd;
		float[] wallStart;
		if (distToTar0 > distToTar1) {
			Panel.drawCircle(Color.GREEN, allWalls[lowWall][1][0],
					allWalls[lowWall][1][1], 6);
			// 0 is farther
			wallEnd = allWalls[lowWall][1];
			wallStart = allWalls[lowWall][0];
		} else {
			Panel.drawCircle(Color.ORANGE, allWalls[lowWall][0][0],
					allWalls[lowWall][0][1], 6);
			// 1 is farther
			wallEnd = allWalls[lowWall][0];
			wallStart = allWalls[lowWall][1];
		}
		float[] walV = VeMa.vectSub(wallEnd, wallStart);
		float walVa = VeMa.norm(walV);
		float[] plaV = VeMa.vectSub(playLoc, wallStart);
		float[] tarV = VeMa.vectSub(firstTar, wallStart);

		float[] plaV1 = VeMa.getA1(plaV, walV);
		float[] tarV1 = VeMa.getA1(tarV, walV);

		Panel.drawCircle(Color.ORANGE, wallStart[0] + tarV1[0], wallStart[1]
				+ tarV1[1], 6);

		float tarWalXrat = tarV1[0] / walV[0];

		// System.out.println("tarV1a: " + tarV1a);
		System.out.println("walVa: " + walVa);
		System.out.println("tarWalXrat: " + tarWalXrat);
		boolean goToEdge = false;
		if (tarWalXrat > 1) {
			// tar past end of wall. scale it to the end.
			tarV1 = walV;
			goToEdge = true;
			System.out.println("goToEdgeNext");
		}
		// if play1WalXrat is greater than 1 or less than 0.
		// curve player.`
		float[] nTarLoc = VeMa.vectAdd(playLoc, VeMa.vectSub(tarV1, plaV1));
		System.out.println("continue");
		Panel.drawCircle(Color.BLUE, firstTar[0], firstTar[1], 6);
		Panel.drawLine(new Color(64, 188, 188), playLoc[0], playLoc[1],
				nTarLoc[0], nTarLoc[1]);
		VeToWa(moveSpeedLeft, nTarLoc, lowWall, killWall, tempTar, firstTar,
				goToEdge);
	}

	void sayVect(String name, float[] vect) {
		System.out.println(name + " (" + vect[0] + ", " + vect[1] + ")");
	}
}
