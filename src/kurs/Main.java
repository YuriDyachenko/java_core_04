package kurs;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static final int SIZE = 5;
    public static final int DOTS_TO_WIN = 4;
    public static final char DOT_EMPTY = ' ';
    public static final char DOT_X = 'X';
    public static final char DOT_O = 'O';
    //победители
    public static final int DEAD_HEAD = 0;
    public static final int HUMAN = 1;
    public static final int AI = 2;
    public static final int BREAK = 3;
    //типы найденных последовательностей: ничего, строка, колонка, главная диагональ, побочная диагональ
    public static final int NONE = 0;
    public static final int ROW = 1;
    public static final int COLUMN = 2;
    public static final int MAIN_DIAG = 3;
    public static final int SIDE_DIAG = 4;
    //в массиве из двух координат 0 - x, 1 - y
    public static final int X = 0;
    public static final int Y = 1;
    //горизонтальная линия для вывода таблицы, чтобы не формировать каждый раз
    public static String horLine;
    //матрица
    public static char[][] map;
    //здесь накапливаем победную последовательность
    //чтобы ее потом выделить при печати
    public static int[][] coordinatesWin;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        horLine = createHorLine();
        map = createMap();
        coordinatesWin = createCoordinates();

        //выводим условия игры
        outGameHeader();
        //первый вывод матрицы
        printMap();

        //изначально - ничья
        int winner = DEAD_HEAD;

        do {

            //если хотят выйти, получим "Вы сдались"
            if (!humanTurn(scanner)) {
                winner = BREAK;
                break;
            }

            //чтобы отобразить победную строку/колонку/ряд
            //нужно сначала проверить, а потом печатать
            int hasWin = checkSome(coordinatesWin, DOT_X, DOTS_TO_WIN);
            printMap();
            if (hasWin != NONE) {
                winner = HUMAN;
                break;
            }
            //может зависнуть, если это был последний ход
            //проверим полное заполнение таблицы
            if (mapIsFull())
                break;

            //второй всегда играет ноликами
            //АИ - все по аналогии, только захотеть выйти он не может
            aiTurn();

            hasWin = checkSome(coordinatesWin, DOT_O, DOTS_TO_WIN);
            printMap();
            if (hasWin != NONE) {
                winner = AI;
                break;
            }

        } while (mapIsNotFull());

        //выводим победителя или ничью
        System.out.printf("%s\n", winner == HUMAN ? "Вы победили!" : winner == AI ? "Победил ИИ!" :
                winner == BREAK ? "Вы сдались... :(" : "Честная НИЧЬЯ!");

        scanner.close();
    }

    private static void outGameHeader() {
        System.out.printf("Играем в крестики-нолики (первый ход - %c)\n", DOT_X);
        System.out.printf("Размер матрицы: %dх%d, число %c|%c для выигрыша: %d\n",
                SIZE, SIZE, DOT_X, DOT_O, DOTS_TO_WIN);
        System.out.println("Вы начинаете...");
    }

    private static int[][] createCoordinates() {
        //в принципе, тут никогда не будет "длинее", чем нужно символов для победы
        int[][] coordinates = new int[DOTS_TO_WIN][2];
        //начальная очистка заполнением {-1, -1}
        clearCoordinates(coordinates);
        return coordinates;
    }

    private static String createHorLine() {
        //создание горизональной линии, чтобы не строить ее каждый раз
        StringBuilder stringBuilder = new StringBuilder("+-----+");
        for (int x = 0; x < SIZE; x++) stringBuilder.append("-----+");
        return stringBuilder.toString();
    }

    private static char[][] createMap() {
        //создание матрицы
        char[][] map = new char[SIZE][SIZE];
        for (char[] row: map)
            Arrays.fill(row, DOT_EMPTY);
        return map;
    }

    private static void putInXY(int[] xy, int x, int y) {
        //просто "ускоряем" размещение двух координат в элементе
        xy[X] = x;
        xy[Y] = y;
    }

    private static void clearXY(int[] xy) {
        //просто очистка
        xy[X] = -1;
        xy[Y] = -1;
    }

    private static void putInCoordinates(int[][] coordinates, int i, int x, int y) {
        //просто "ускоряем" размещение двух координат в массиве на нужном месте
        putInXY(coordinates[i], x, y);
    }

    private static boolean isEmptyXY(int[] xy) {
        //достаточно проверить X или Y на -1
        return xy[Y] == -1;
    }

    private static int findEmptyInCoordinates(int[][] coordinates) {
        //ищем первую ячейку пустую
        for (int i = 0; i < coordinates.length; i++) {
            if (isEmptyXY(coordinates[i]))
                return i;
        }
        //-1 - признак, что больше нет места
        return -1;
    }

    private static void printMap() {
        //вывод на печати в "рамочках", с координатами
        //координаты "победы" нужны, чтобы отметить "победные" символы
        System.out.println(horLine);

        //выводим строку с координатами x
        System.out.print("|     |");
        for (int x = 0; x < SIZE; x++) {
            System.out.printf(" x=%d%s|", x + 1, x > 8 ? "" : " ");
        }
        System.out.println();

        //выводим матрицу, но слева сначала координаты y
        for (int y = 0; y < SIZE; y++) {
            System.out.println(horLine);
            System.out.printf("| y=%d%s|", y + 1, y > 8 ? "" : " ");
            for (int x = 0; x < SIZE; x++) {
                //если это именно победивший набор коородинат
                boolean b = isInCoordinates(coordinatesWin, x, y);
                //то выводим маркеры, чтобы было понятно, по какому именно набору
                //программа определила победу
                System.out.printf("%c %c %c|", b ? '>' : ' ', charFromMap(x, y), b ? '<' : ' ');
            }
            System.out.println();
        }

        System.out.println(horLine);
    }

    private static void clearCoordinates(int[][] coordinates) {
        //заполняем последовательность координатами {-1, -1}

        //обработаем и случай, когда тут пусто совсем
        if (coordinates.length == 0)
            return;

        //нет смысла чистить/заполнять, если в первом же элементе уже {-1, -1}
        if (isEmptyXY(coordinates[0]))
            return;

        for (int i = 0; i < coordinates.length; i++) {
            clearXY(coordinates[i]);
        }
    }

    private static boolean isInCoordinates(int[][] coordinates, int x, int y) {
        //проверка наличия конкретных координат в массиве победных координат
        //чтобы вывести в той ячейке специальные маркеры
        for (int[] ints : coordinates) {
            if (ints[X] == x && ints[Y] == y)
                return true;
        }
        return false;
    }

    private static char charFromMap(int x, int y) {
        //один метод для получения символа из матрицы
        return map[y][x];
    }

    private static void putToMap(char c, int x, int y) {
        //один метод для установки значения в ячейке матрицы
        map[y][x] = c;
    }

    private static void putToMap(char c, int[] xy) {
        //перегруженный метод, согда сразу {X, Y} приходит
        putToMap(c, xy[X], xy[Y]);
    }

    private static boolean putInCoordinatesWithCheck(int[][] coordinates, int x, int y, char charWin,
                                                     int dotsForWin, int typeOfCoordinates) {
        //идея такая - иначально массив координат заполнен {-1, -1}
        //мы можем проверять, где еще свободно и автоматически
        //находить место для записи текущей координаты
        //если при этом смотреть, какой символ получается в этих координатах
        //то можно вернуть что-то заранее, поняв, что больше не нужно
        //возвращать будем ИСТИНУ, если накопилось нужное число символов
        //добавлен dotsForWin, потому что не всегда ищем победную последовательность, но и для блокировки тоже
        //для чего будем смотреть по dotsForWin, если равна DOTS_TO_WIN, то это для победы
        //иначе - для блокировки и в этом случае мы будем обязательно проверять, свободна слева
        //или справа координата

        int place = findEmptyInCoordinates(coordinates);
        //может вернуться -1, если уже нет места
        //такое вряд ли будет, но обработаем этот вариант
        if (place == -1) return false;

        //символ по пришедшим координатам
        char c = charFromMap(x, y);

        //обычный режим или это блокировка/попытка ИИ победить
        boolean blockMode = dotsForWin < DOTS_TO_WIN;

        if (c == charWin) {
            //"пришел" нужный символ
            //мы помещаем в массив только нужные
            //поэтому смело его помещаем на место
            putInCoordinates(coordinates, place, x, y);
            //если накопилось нужное число, то больше и не копим
            //изначально тут было DOTS_TO_WIN, но мы используем этот алгоритм для поиска
            //последовательности любого числа символов для последующей блокировки
            if (place + 1 == dotsForWin) {
                //вот тут, если мы нашли нужное число символов
                //для именно блокировки нужно проверить точку слева и справа
                //если есть свободная, тогда нашли, иначе ищем дальше
                if (blockMode) {
                    //именно это режим блокировки слева или справа
                    //нам нужно, чтобы слева или справа было пусто
                    int[] xy = leftDot(coordinates, typeOfCoordinates);
                    //если есть слева пустое место, это нам подходит
                    if (!isEmptyXY(xy)) return true;
                    //иначе смотрим справа
                    xy = rightDot(coordinates, typeOfCoordinates);
                    //если есть справа пустое место, это нам подходит
                    if (!isEmptyXY(xy)) return true;
                    //такая последовательность нам не подходит
                    //чистим ее и ищем дальше
                    clearCoordinates(coordinates);
                    return false;
                }
                return true;
            }

            //если какое-то количество накопили, но недостаточно
            //мы уже не узнаем, будут еще символы или нет,
            //значит, не сможем очистить массив здесь, сделаем это там, где вызывается этот метод
        } else {
            //"пришел" ненужный символ
            //нужно не только НЕ помещать его в массив
            //но и стереть то, что уже там накопилось
            clearCoordinates(coordinates);
        }
        return false;
    }

    private static int checkSome(int[][] coordinates, char charWin, int dotsForWin) {
        //нужный для победы символ в charWin
        //массив координат всегда очищен (заполнен {-1, -1})
        //dotsForWin - сколько искать символов, для победы будет "победное", я для блокировки - другое
        //изначально возвращала булево - нет или есть такая, но нам нужно знать, ряд нашелся или что другое
        //0 - ничего, 1 - строка, 2 - колонка, 3 - главная диагональ, 4 - побочная диагональ

        //все строки
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                //последовательно помещаем координаты в массив, там копятся только нужные
                //координаты, если накопилось нужно количество - выиграли
                if (putInCoordinatesWithCheck(coordinates, x, y, charWin, dotsForWin, ROW))
                    return ROW;
            }
            //чистим массив победных координат для повторного использования
            clearCoordinates(coordinates);
        }

        //все колонки
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (putInCoordinatesWithCheck(coordinates, x, y, charWin, dotsForWin, COLUMN))
                    return COLUMN;
            }
            clearCoordinates(coordinates);
        }

        //максимальный сдвиг для дополнительных диагоналей
        int maxShift = SIZE - DOTS_TO_WIN;

        for (int dy = -maxShift; dy <= maxShift; dy++) {
            //главная диагональ
            for (int y = 0; y < SIZE; y++) {
                int x = y - dy;
                //может получиться "вне" нашей матрицы, потому что через dy мы
                //ее сдвигаем вверх и вниз виртуально, поэтому добавляем проверку
                if (x < 0 || x >= SIZE) continue;
                if (putInCoordinatesWithCheck(coordinates, x, y, charWin, dotsForWin, MAIN_DIAG))
                    return MAIN_DIAG;
            }
            clearCoordinates(coordinates);
            //побочная диагональ
            for (int y = 0; y < SIZE; y++) {
                int x = SIZE - 1 - y - dy;
                if (x < 0 || x >= SIZE) continue;
                if (putInCoordinatesWithCheck(coordinates, x, y, charWin, dotsForWin, SIDE_DIAG))
                    return SIDE_DIAG;
            }
            clearCoordinates(coordinates);
        }

        //если ничего не нашлось...
        return NONE;
    }

    private static boolean mapIsNotFull() {
        //если найдется хотя бы одна пустая ячейка, значит, матрица еще не заполнена
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (charFromMap(x, y) == DOT_EMPTY)
                    return true;
            }
        }
        //иначе - заполнена полностью
        return false;
    }

    private static boolean mapIsFull() {
        return !mapIsNotFull();
    }

    private static boolean humanTurn(Scanner scanner) {
        //ход человека
        int x, y;

        do {

            System.out.printf("Введите X Y (две координаты от 1 до %d через пробел, 0 для выхода): ", SIZE);
            x = scanner.nextInt() - 1;
            //если хотят выйти, вернем ЛОЖЬ
            if (x == -1) return false;
            y = scanner.nextInt() - 1;

        } while (isNotCellValid(x, y));

        //ставим точку
        putToMap(DOT_X, x, y);
        System.out.printf("Ваш \"ход\" сделан в ячейку {%d, %d}\n", x + 1, y + 1);

        //если ход сделан, вернем ИСТИНУ
        return true;
    }

    private static void aiTurn() {
        //ход ИИ

        //получается, что ИИ все время только блокирует, а если у него есть
        //последовательность, где достаточно поставить один символ и будет победа
        //то это игнорируется
        //попробуем исправить, сначала проверяем DOTS_TO_WIN - 1 последовательность с местом
        //слева или справа и закрываем ее
        int[] xy = aiTryWin(DOT_O);
        if (!isEmptyXY(xy)) {
            System.out.printf("ИИ побеждает, ставя %c в ячейку {%d, %d}\n", DOT_O, xy[X] + 1, xy[Y] + 1);
            return;
        }

        //вместо случайных координат
        //мы сначала попробуем подобрать свои для блокировки
        //передаем тот символ победы, которые ему нужно поставить
        //там же он сам его и проставит
        //а блокировать он будет "другой"
        xy = aiBlock(DOT_O, false);
        if (!isEmptyXY(xy)) {
            System.out.printf("ИИ ставит блок в ячейку {%d, %d}\n", xy[X] + 1, xy[Y] + 1);
            return;
        }

        //еще один нерандомный вариант, когда нет явной победы за один ход
        //и нечего срочно блокировать
        //нужно в своих последовательностях, начиная с "победные минус 2"
        //пробовать поставить рядом, а не делать рандомный ход
        xy = aiNoRandom(DOT_O);
        if (!isEmptyXY(xy)) {
            System.out.printf("ИИ осознанно \"ходит\" в ячейку {%d, %d}\n", xy[X] + 1, xy[Y] + 1);
            return;
        }

        int x, y;
        do {

            x = new Random().nextInt(SIZE);
            y = new Random().nextInt(SIZE);

        } while (isNotCellValid(x, y));

        putToMap(DOT_O, x, y);
        System.out.printf("ИИ \"ходит\" рандомно в ячейку {%d, %d}\n", x + 1, y + 1);
    }

    private static int[] leftDot(int[][] coordinates, int typeOfCoordinates) {
        //общая функция по определению точки слева для найденной последовательности
        //в ней же мы проверим на пустоту, если занята, вернем {-1, -1}
        int[] xy = createXY();
        //начальный элемент массива
        int[] xyIn = coordinates[0];

        int x = xyIn[X];
        int y = xyIn[Y];
        //для строки нам нужна точка слева
        if (typeOfCoordinates == ROW)
            x--;
        //для колонки нужна точка выше
        if (typeOfCoordinates == COLUMN)
            y--;
        //для главной диагонали нужна точка выше/левее
        if (typeOfCoordinates == MAIN_DIAG) {
            x--;
            y--;
        }
        //для побочной диагонали нужна точка ниже/левее
        //было бы так, но у нас последовательность фомируется сверху вниз
        //поэтому левая имеет смысл "перед первой"
        //и мы будем брать выше/правее
        if (typeOfCoordinates == SIDE_DIAG) {
            x++;
            y--;
        }
        //если точка прошла все проверки и пустая, берем ее
        if (!isNotCellValid(x, y))
            putInXY(xy, x, y);
        //иначе вернем пустую {-1, -1}
        return xy;
    }

    private static int[] rightDot(int[][] coordinates, int typeOfCoordinates) {
        //общая функция по определению точки справа для найденной последовательности
        //в ней же мы проверим на пустоту, если занята, вернем {-1, -1}
        int[] xy = createXY();

        //найдем последний заполненный элемент массива, сначала - его размер
        int sizeBlock = findEmptyInCoordinates(coordinates);
        if (sizeBlock == -1) sizeBlock = coordinates.length;
        int[] xyIn = coordinates[sizeBlock - 1];

        int x = xyIn[X];
        int y = xyIn[Y];
        //для строки нам нужна точка справа
        if (typeOfCoordinates == ROW)
            x++;
        //для колонки нужна точка ниже
        if (typeOfCoordinates == COLUMN)
            y++;
        //для главной диагонали нужна точка ниже/правее
        if (typeOfCoordinates == MAIN_DIAG) {
            x++;
            y++;
        }
        //для побочной диагонали нужна точка выше/правее
        //было бы так, но у нас последовательность фомируется сверху вниз
        //поэтому правая имеет смысл "после последней"
        //и мы будем брать ниже/левее
        if (typeOfCoordinates == SIDE_DIAG) {
            x--;
            y++;
        }
        //если точка прошла все проверки и пустая, берем ее
        if (!isNotCellValid(x, y))
            putInXY(xy, x, y);
        //иначе вернем пустую {-1, -1}
        return xy;
    }

    private static int[] createXY() {
        int[] xy = new int[2];
        clearXY(xy);
        return xy;
    }

    private static int[] putInMapLeftOrRight(char charWin, int[][] coordinates, int typeOfCoordinates) {
        //у нас есть функция получения точки слева, причем она сразу проверяется на пустоту
        int[] xy = leftDot(coordinates, typeOfCoordinates);
        //если подходит, сразу в ней и ставим
        if (!isEmptyXY(xy)) {
            putToMap(charWin, xy);
        } else {
            //иначе берем ту, что справа
            xy = rightDot(coordinates, typeOfCoordinates);
            if (!isEmptyXY(xy))
                putToMap(charWin, xy);

        }
        //всегда возвращаем какую-то точку, она будет пустой или заполненной
        return xy;
    }

    private static int[] aiNoRandom(char charWin) {
        //как мне кажется, осознанный выбор не чем не отличается от
        //блокировки, только искать нужно свои же символы и ставить свои
        //это простой алгоритм, сложнее - это когда уже начиная со второго
        //символа, АИ пытается правильно его поставить, прикидывая, хватит ли места "с той стороны"
        //для выигрышной последовательности
        //да и первый символ можно ставить осознанно, чтобы было место тоже для всех остальных
        //это все здесь не реализуется, потому что так можно придумывать до бесконечности :)
        return aiBlock(charWin, true);
    }

    private static int[] aiTryWin(char charWin) {
        int[][] coordinates = createCoordinates();
        //проверяем, нет ли такой последовательности, где до победы останется всего один символ
        //и его можно поставить слева или справа
        int hasWin = checkSome(coordinates, charWin, DOTS_TO_WIN - 1);
        if (hasWin == NONE) return createXY();
        //здесь вызывается повторяющийся блок кода, для блокировки он такой и для попытки ИИ победить
        return putInMapLeftOrRight(charWin, coordinates, hasWin);
    }

    private static int[] aiBlock(char charWin, boolean noRandom) {
        //будем искать возможность блокировки, если до победы у противника остается 2 шага
        int minDotsToBlock = DOTS_TO_WIN - 2;
        //для матрицы 3х3 все равно ищем 2
        if (minDotsToBlock == 1) minDotsToBlock = 2;
        //если получился 0, вообще не ищем ничего, но нужно вернуть "пустую" точку
        if (minDotsToBlock == 0) return createXY();
        //блокировать нужно символы чужие, т.е. "наоборот"
        char charBlock = noRandom ? charWin : charWin == DOT_O ? DOT_X : DOT_O;
        //сначала нужно поискать последовательность подлиннее, а потом уже, если
        //такая не нашлась, то блокировать более короткую
        //для осознанного хода, а не блокировки (флаг noRandom)
        //нам не нужно проверять DOTS_TO_WIN - 1, потому что он уже проверен в "победной" проверке aiTryWin
        int start = noRandom ? DOTS_TO_WIN - 2 : DOTS_TO_WIN - 1;

        for (int dotsToBlock = start; dotsToBlock >= minDotsToBlock; dotsToBlock--) {
            int[][] coordinates = createCoordinates();
            //если нет такой последовательности, то делаем рандомый шаг
            int hasBlock = checkSome(coordinates, charBlock, dotsToBlock);
            if (hasBlock == NONE) continue;
            //здесь вызывается повторяющийся блок кода, и для блокировки он такой и для попытки ИИ победить
            int[] xy = putInMapLeftOrRight(charWin, coordinates, hasBlock);
            if (isEmptyXY(xy)) continue;
            return xy;
        }
        //вернем пусто
        return createXY();
    }

    private static boolean isNotCellValid(int x, int y) {
        //проверка на правильность координат и на пустоту внутри ячейки
        return !(x >= 0 && x < SIZE && y >= 0 && y < SIZE && charFromMap(x, y) == DOT_EMPTY);
    }

}
