package cn.edu.nottingham.hnyzx3.mp3player.utils;

import android.graphics.Color;

import java.util.Objects;
import java.util.Random;


public class ColorFactory {

    /**
     * randomly generate a color
     */
    public static int getRandomColor() {
        Random random = new Random();
        int x = random.nextInt(Objects.requireNonNull(Colors.class.getEnumConstants()).length);
        Colors color = Objects.requireNonNull(Colors.class.getEnumConstants())[x];
        return color.getColor();
    }

    public enum Colors {
        DKGREY(Color.DKGRAY),
        GRAY(Color.GRAY),
        WHITE(Color.WHITE),
        RED(Color.BLACK),
        GREEN(Color.GREEN),
        BLUE(Color.BLUE),
        YELLOW(Color.YELLOW),
        MAGENTA(Color.MAGENTA);

        private final int color;

        Colors(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }
}