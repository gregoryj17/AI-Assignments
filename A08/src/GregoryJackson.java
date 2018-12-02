import java.util.*;

//An amalgamation of various things I liked from example bots
class GregoryJackson implements IAgent
{
	int iter;
	int index; // a temporary value used to pass values around
	Random r = new Random();

	GregoryJackson() {
		reset();
	}

	public void reset() {
		iter = 0;
	}

	/*
	// These functions are general utilities shared by all example bots
	*/


	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	float nearestBombTarget(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getBombCount(); i++) {
			float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	float nearestOpponent(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 0.5f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;
			m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
		}
	}

	/*
	// beDefender is adapted from Mixed bot for defending purposes
	 */

	void beDefender(Model m, int i) {
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			// Stay between the enemy and my flag
			m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

			// Throw bombs if the enemy gets close enough
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				m.throwBomb(i, enemyX, enemyY);
		}
		else {
			// Guard the flag
			m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		}

		// If I don't have enough energy to throw a bomb, rest
		if(m.getEnergySelf(i) < Model.BOMB_COST*1.5)
			m.setDestination(i, m.getX(i), m.getY(i));

		// Try not to die
		avoidBombs(m, i);
	}

	/*
	// update is the meat of the program, with pieces adapted from aggressivepack, mixed, and prescientmoron
	 */
	public void update(Model m) {
		// Come together at the start of the game
		if(iter < 200) {
			m.setDestination(0, 300, 300);
			m.setDestination(1, 300, 300);
			m.setDestination(2, 300, 300);
			iter++;
			return;
		}

		// Find my player with the most energy.
		int leader = 0;
		if(m.getEnergySelf(1) > m.getEnergySelf(leader))
			leader = 1;
		if(m.getEnergySelf(2) > m.getEnergySelf(leader))
			leader = 2;

		// Find the enemy closest to the leader
		nearestOpponent(m, m.getX(leader), m.getY(leader));
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			for(int i = 0; i < 3; i++) {

				// Get close enough to throw a bomb at the enemy
				float myX = m.getX(i);
				float myY = m.getY(i);
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				//pickDestination(m, i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
				//a_star(m, i, (int)(enemyX), (int)(enemyY));

				// Throw bombs if I can hit the enemy
				if(sq_dist(enemyX, enemyY, myX, myY) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
				else{
					beDefender(m, i);
				}
			}
		}
		else {
			for(int i = 0; i < 3; i++) {

				// Head for the opponent's flag
				//m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
				pickDestination(m, i, Model.XFLAG_OPPONENT - (7 * Model.MAX_THROW_RADIUS / 8), Model.YFLAG_OPPONENT);

				// Shoot at the flag if I can hit it
				if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
					if(iter % 5 == 0)
						m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
				}
			}
		}

		// Try not to die
		for(int i = 0; i < 3; i++)
			avoidBombs(m, i);
		iter++;
	}

	/*
	// pickDestination is adapted from prescientmoron to find a good path to a destination
	 */

	void pickDestination(Model m, int sprite, float xdest, float ydest) {

		float bestX = m.getX(sprite);
		float bestY = m.getY(sprite);
		float best_sqdist = sq_dist(bestX, bestY, xdest, ydest);
		for(int sim = 0; sim < 10; sim++) { // try 10 candidate destinations

			// Pick a random destination to simulate
			float x = (float)r.nextDouble() * Model.XMAX;
			float y = (float)r.nextDouble() * Model.YMAX;

			// Fork the universe and simulate it for 10 time-steps
			Controller cFork = m.getController().fork(new MyShadow(x, y), new OpponentShadow());
			Model mFork = cFork.getModel();
			for(int j = 0; j < 10; j++)
				cFork.update();

			// See how close the current sprite got to the opponent's flag in the forked universe
			float sqd = sq_dist(mFork.getX(sprite), mFork.getY(sprite), xdest, ydest);
			if(sqd < best_sqdist) {
				best_sqdist = sqd;
				bestX = x;
				bestY = y;
			}
		}
		for(int i = 0; i < 8; i++) { // try 8 candidate destinations
			float x = m.getX(sprite);
			float y = m.getY(sprite);

			if(i<3)y-=20;
			if(i>3&&i<7)y+=20;
			if(i<1||i>5)x-=20;
			if(i>1&&i<5)x+=20;

			// Fork the universe and simulate it for 10 time-steps
			Controller cFork = m.getController().fork(new MyShadow(x, y), new OpponentShadow());
			Model mFork = cFork.getModel();
			for(int j = 0; j < 10; j++)
				cFork.update();

			// See how close the current sprite got to the opponent's flag in the forked universe
			float sqd = sq_dist(mFork.getX(sprite), mFork.getY(sprite), xdest, ydest);
			if(sqd < best_sqdist) {
				best_sqdist = sqd;
				bestX = x;
				bestY = y;
			}
		}

		// Head for the point that worked out best in simulation
		m.setDestination(sprite, bestX, bestY);

		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(sprite), m.getY(sprite), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
			m.throwBomb(sprite, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
	}

	/*
	// the shadow classes are adapted from prescientmoron for use with pickDestination
	 */

	static class MyShadow implements IAgent
	{
		float dx;
		float dy;

		MyShadow(float destX, float destY) {
			dx = destX;
			dy = destY;
		}

		public void reset() {
		}

		public void update(Model m) {
			for(int i = 0; i < 3; i++) {
				m.setDestination(i, dx, dy);
			}
		}
	}

	static class OpponentShadow implements IAgent
	{
		OpponentShadow() {
		}

		public void reset() {
		}

		public void update(Model m) {
			// The imagined opponent does nothing
		}
	}

}
