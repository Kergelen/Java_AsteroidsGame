import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class AsteroidsGame extends JPanel implements ActionListener, KeyListener {
    public static final int width = 1000;
    public static final int height = 600;

    private int score = 0;
    private int highscore = 0;
    private boolean gameOver = false;
    private boolean[] keys;
    private Timer timer;
    private Spaceship spaceship;
    private ArrayList<Bullet> bullets;
    private ArrayList<Asteroid> asteroids;

    public AsteroidsGame() 
    {
        keys = new boolean[256];
        timer = new Timer(15, this);
        spaceship = new Spaceship(width / 2, height / 2);
        bullets = new ArrayList<>();
        asteroids = new ArrayList<>();

        addKeyListener(this);
        setFocusable(true);
        timer.start();
        spawnAsteroids();

        setVisible(true);
    }

    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Asteroids Game");
            AsteroidsGame game = new AsteroidsGame();
            frame.add(game);
            frame.setSize(width, height);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }

    private void spawnAsteroids() 
    {
        Random rand = new Random();
        int numberOfAsteroids = 1 + rand.nextInt(3);
        for (int i = 0; i < numberOfAsteroids; i++) 
        {
            int side = rand.nextInt(4);
            int x = 0, y = 0;

            switch (side) 
            {
                case 0:
                    x = rand.nextInt(width);
                    y = 0;
                    break;
                case 1:
                    x = width;
                    y = rand.nextInt(height);
                    break;
                case 2:
                    x = rand.nextInt(width);
                    y = height;
                    break;
                case 3:
                    x = 0;
                    y = rand.nextInt(height);
                    break;
            }
            int speed = 2 + rand.nextInt(4);
            int direction = rand.nextInt(360);

            asteroids.add(new Asteroid(x, y, speed, direction));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        update();
        repaint();
        if (gameOver) 
        {
            int result = JOptionPane.showConfirmDialog(this, "Koniec Gry! Zagrać ponownie?", "Koniec gry", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) 
            {
                resetGame();
            } 
            else 
            {
                System.exit(0);
            }
        }
    }

    private void update() 
    {
        if (!gameOver) {
            spaceship.move(keys);
            updateBullets();
            updateAsteroids();
            checkCollisions();
        }
    }

    private void updateBullets() 
    {
        for (int i = bullets.size() - 1; i >= 0; i--) 
        {
            Bullet bullet = bullets.get(i);
            bullet.move();
            if (bullet.isOutOfBounds()) 
            {
                bullets.remove(i);
            }
        }
    }

    private void updateAsteroids() 
    {
        for (Asteroid asteroid : asteroids) 
        {
            asteroid.move();
        }
    }

    private void checkCollisions() 
    {

        if (!gameOver) 
        {
            for (int j = asteroids.size() - 1; j >= 0; j--) 
            {
                Asteroid asteroid = asteroids.get(j);
                if (spaceship.intersects(asteroid)) 
                {
                    gameOver = true;
                    break;
                }
            }
        }

        for (int i = bullets.size() - 1; i >= 0; i--) 
        {
            Bullet bullet = bullets.get(i);
            for (int j = asteroids.size() - 1; j >= 0; j--) 
            {
                Asteroid asteroid = asteroids.get(j);
                if (bullet.intersects(asteroid)) 
                {
                    bullets.remove(i);
                    asteroids.remove(j);
                    spawnAsteroids();
                    score += 10;
                    break;
                }
            }
        }
    }

    private void resetGame() 
    {
        if (score > highscore) 
        {
        highscore = score;
        }
        score = 0;
        gameOver = false;
        spaceship = new Spaceship(width / 2, height / 2);
        bullets.clear();
        asteroids.clear();
        spawnAsteroids();
    }

    @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        drawStars(g);
        spaceship.draw(g);

        for (Bullet bullet : bullets) 
        {
            bullet.draw(g);
        }
        for (Asteroid asteroid : asteroids) 
        {
            asteroid.draw(g);
        }

        Font font = new Font("Arial", Font.PLAIN, 20); 
        g.setFont(font);
        g.setColor(Color.RED);
        g.drawString("Score: " + score, 10, 20);

        g.setColor(Color.RED);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Highscore: " + highscore, 10, 40);

        if (gameOver) 
        {
            Font fontg = new Font("Arial", Font.BOLD, 40);
            g.setFont(fontg);
            g.setColor(Color.RED);
            g.drawString("Koniec gry!", width / 2 - 100, height / 4 - 50);
            g.drawString("Wynik: " + score, width / 2 - 100, height / 4);
        }
    }

    private void drawStars(Graphics g) 
    {
        Random rand = new Random();
        for (int i = 0; i < 20; i++) 
        {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            g.setColor(Color.WHITE);
            g.fillRect(x, y, 2, 2);
        }   
    }

    @Override
    public void keyPressed(KeyEvent e) 
    {
        keys[e.getKeyCode()] = true;

        if (gameOver && e.getKeyCode() == KeyEvent.VK_ENTER) 
        {
            resetGame();
        } 
        else if (!gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) 
        {
            bullets.add(new Bullet(spaceship.getX(), spaceship.getY(), spaceship.getDirection()));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) 
    {
        keys[e.getKeyCode()] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

class Spaceship 
{
    private int x;
    private int y;
    private int direction;

    public Spaceship(int x, int y) 
    {
        this.x = x;
        this.y = y;
        this.direction = 0;
    }

    public int getX() 
    {
        return x;
    }

    public int getY() 
    {
        return y;
    }

    public int getDirection() 
    {
        return direction;
    }

    public void move(boolean[] keys) 
    {
        if (keys[KeyEvent.VK_LEFT]) 
        {
            direction -= 5;
        }
        if (keys[KeyEvent.VK_RIGHT]) 
        {
            direction += 5;
        }
        if (keys[KeyEvent.VK_UP]) 
        {
            x += 5 * Math.cos(Math.toRadians(direction));
            y += 5 * Math.sin(Math.toRadians(direction));
        }
    }

    public void draw(Graphics g) 
    {
        g.setColor(Color.BLUE);
        int[] xPoints = {x, x + 20, x - 20};
        int[] yPoints = {y, y + 30, y + 30};
        Graphics2D g2d = (Graphics2D) g;
        g2d.rotate(Math.toRadians(direction), x, y);
        g2d.fillPolygon(xPoints, yPoints, 3);
        g2d.rotate(-Math.toRadians(direction), x, y);
    }

    public boolean intersects(Asteroid asteroid) 
    {
        Rectangle spaceshipRect = new Rectangle(x, y, 20, 30);
        Rectangle asteroidRect = new Rectangle(asteroid.getX(), asteroid.getY(), 30, 30);
        return spaceshipRect.intersects(asteroidRect);
    }
}

class Bullet 
{
    private int x;
    private int y;
    private int direction;

    public Bullet(int x, int y, int direction) 
    {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public void move() 
    {
        x += 10 * Math.cos(Math.toRadians(direction));
        y += 10 * Math.sin(Math.toRadians(direction));
    }

    public boolean isOutOfBounds() 
    {
        return x < 0 || x > AsteroidsGame.width || y < 0 || y > AsteroidsGame.height;
    }

    public void draw(Graphics g) 
    {
        g.setColor(Color.RED);
        g.fillOval(x, y, 10, 10);
    }

    public boolean intersects(Asteroid asteroid) 
    {
        Rectangle bulletRect = new Rectangle(x, y, 5, 5);
        Rectangle asteroidRect = new Rectangle(asteroid.getX(), asteroid.getY(), 30, 30);
        return bulletRect.intersects(asteroidRect);
    }
}

class Asteroid 
{
    private int x;
    private int y;
    private int speed;
    private int direction;

    private Color baseColor;
    private Color borderColor;

    public Asteroid(int x, int y, int speed, int direction) 
    {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.direction = direction;
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        this.baseColor = new Color(r, g, b);
        this.borderColor = baseColor.darker();
    }

    public void move() 
    {
        x += speed * Math.cos(Math.toRadians(direction));
        y += speed * Math.sin(Math.toRadians(direction));

        if (x > AsteroidsGame.width) 
        {
            x = 0;
        } 
        else if (x < 0) 
        {
            x = AsteroidsGame.width;
        }

        if (y > AsteroidsGame.height) 
        {
            y = 0;
        } 
        else if (y < 0) 
        {
            y = AsteroidsGame.height;
        }
    }

    public int getX() 
    {
        return x;
    }

    public int getY() 
    {
        return y;
    }

    public void draw(Graphics g) 
    {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(borderColor);
        g2d.fillOval(x, y, 30, 30);

        g2d.setColor(baseColor);
        g2d.fillOval(x, y, 25, 25);
    }
}
