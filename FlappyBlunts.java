import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;


public class FlappyBlunts extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 360;
    int boardHeight = 640;

    //Images
    Image backgroundImg;
    Image daveBluntsImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //Blunts
    int bluntX = boardWidth / 8;
    int bluntY = boardHeight / 2;
    int bluntWidth = 34*2;
    int bluntHeight = 24*2;

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    //Only use this one
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -10;
        }
        if (gameOver) {
            //restart the game
            blunts.y = bluntY;
            velocityY = -10;
            pipes.clear();
            score = 0;
            gameLoop.start();
            gameOver = false;
            placePipesTimer.start();
            playSound("C:/Users/victo/OneDrive/Desktop/FlappyBlunts/FlappyBlunts/src/bgm.wav");

        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    class Blunts {
        int x = bluntX;
        int y = bluntY;
        int height = bluntHeight;
        int width = bluntWidth;
        Image img;
        Blunts(Image img) {
            this.img = img;
        }
    }
    //Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;
    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    //game logic
    Blunts blunts;
    int velocityX = -4;
    int velocityY = -10;
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();
    Timer gameLoop;
    Timer placePipesTimer;
    boolean gameOver = false;
    double score = 0;

    FlappyBlunts() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));

        setFocusable(true);
        addKeyListener(this);
        //load Images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        daveBluntsImg = new ImageIcon(getClass().getResource("./blunts.png")).getImage();
        topPipeImg  = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        //bird
        blunts = new Blunts(daveBluntsImg);
        pipes = new ArrayList<Pipe>();
        //place pipes timer
        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipesTimer.start();
        //game timer
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
        playSound("C:/Users/victo/OneDrive/Desktop/FlappyBlunts/FlappyBlunts/src/bgm.wav");
    }
    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            draw(g);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
    public void draw(Graphics g) throws LineUnavailableException {
        //Background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        //Dave blunts
        g.drawImage(blunts.img, blunts.x, blunts.y, blunts.width, blunts.height, null);
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        //score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        if (gameOver) {

            g.drawString("Game Over: " + String.valueOf((int) score), (boardWidth / 4) - 12, (boardHeight / 2) - 20);
            g.drawString("Press the spacebar", (boardWidth / 4) - 50, (boardHeight / 2) + 40);
            g.drawString("to restart", (boardWidth / 4) + 20, (boardHeight / 2) + 80);
            stopSound();
        }
        else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }
    public void move(){
        //bird
        velocityY += gravity;
        blunts.y += velocityY;
        blunts.y = Math.max(blunts.y, 0);

        //pipes
        for(int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && blunts.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5; // 0.6 due to there being 2 pipes that are passed
            }

            if (collision(blunts, pipe)) {
                gameOver = true;
            }
        }
        if (blunts.y > boardHeight) {
            gameOver = true;
        }
        if (blunts.y == 0 && !clip.isRunning()) {
            //playSound("C:/Users/victo/OneDrive/Desktop/FlappyBlunts/FlappyBlunts/src/boonk.wav");
        }

    }
    public boolean collision(Blunts a, Pipe b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }
    //Sound
    public static Clip clip;
    public static void playSound(String filePath) {
        try {
            // Open an audio input stream
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            // Get a sound clip resource
            clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Start the clip in a loop
            //clip.loop(Clip.LOOP_CONTINUOUSLY);

            // Play the clip
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }

    }
    public static void stopSound() throws LineUnavailableException {
        if (clip != null && clip.isRunning()) {
            clip.stop(); // Stop the audio
            clip.setFramePosition(0); // Optional: Reset to the beginning
        }
    }
}