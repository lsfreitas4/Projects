import java.util.Arrays;
import java.util.Scanner;

public class MatrixMultiplication {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int matrixSize, op;

        do {
            System.out.println("\n1. Multiplication");
            System.out.println("2. Line Multiplication");
            System.out.println("3. Block Multiplication");
            System.out.println("0. Exit");
            System.out.print("Selection?: ");
            op = scanner.nextInt();

            if (op == 0) break;

            System.out.print("Enter the matrix size: ");
            matrixSize = scanner.nextInt();

            switch (op) {
                case 1 -> onMult(matrixSize, matrixSize);
                case 2 -> onMultLine(matrixSize, matrixSize);
                case 3 -> {
                    System.out.print("Enter block size: ");
                    int blockSize = scanner.nextInt();
                    onMultBlock(matrixSize, matrixSize, blockSize);
                }
                default -> System.out.println("Invalid option. Please try again.");
            }

            System.out.println("-------------------------------------");
        } while (op != 0);

        scanner.close();
    }

    static void onMult(int m_ar, int m_br) {
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_br];
        double[] phc = new double[m_ar * m_br];

        Arrays.fill(pha, 1.0);
        for (int i = 0; i < m_br; i++) {
            for (int j = 0; j < m_br; j++) {
                phb[i * m_br + j] = i + 1;
            }
        }

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < m_ar; i++) {
            for (int j = 0; j < m_br; j++) {
                double temp = 0;
                for (int k = 0; k < m_ar; k++) {
                    temp += pha[i * m_ar + k] * phb[k * m_br + j];
                }
                phc[i * m_ar + j] = temp;
            }
        }

        printResults(phc, m_br, startTime);
    }

    static void onMultLine(int m_ar, int m_br) {
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_ar];
        double[] phc = new double[m_ar * m_ar];

        Arrays.fill(pha, 1.0);
        for (int i = 0; i < m_br; i++) {
            for (int j = 0; j < m_br; j++) {
                phb[i * m_br + j] = i + 1;
            }
        }

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < m_ar; i++) {
            for (int k = 0; k < m_ar; k++) {
                for (int j = 0; j < m_br; j++) {
                    phc[i * m_ar + j] += pha[i * m_ar + k] * phb[k * m_br + j];
                }
            }
        }

        printResults(phc, m_br, startTime);
    }

    static void onMultBlock(int m_ar, int m_br, int bkSize) {
        double[] pha = new double[m_ar * m_ar];
        double[] phb = new double[m_ar * m_br];
        double[] phc = new double[m_ar * m_br];

        Arrays.fill(pha, 1.0);
        for (int j = 0; j < m_br; j++) {
            for (int k = 0; k < m_br; k++) {
                phb[j * m_br + k] = j + 1;
            }
        }

        long startTime = System.currentTimeMillis();

        for (int ii = 0; ii < m_ar; ii += bkSize) {
            for (int jj = 0; jj < m_br; jj += bkSize) {
                for (int kk = 0; kk < m_ar; kk += bkSize) {
                    for (int i = ii; i < Math.min(ii + bkSize, m_ar); i++) {
                        for (int j = jj; j < Math.min(jj + bkSize, m_br); j++) {
                            double temp = 0;
                            for (int k = kk; k < Math.min(kk + bkSize, m_ar); k++) {
                                temp += pha[i * m_ar + k] * phb[k * m_br + j];
                            }
                            phc[i * m_br + j] += temp;
                        }
                    }
                }
            }
        }

        printResults(phc, m_br, startTime);
    }

    private static void printResults(double[] phc, int m_br, long startTime) {
        long endTime = System.currentTimeMillis();
        System.out.printf("Time: %3.3f seconds\n", (double) (endTime - startTime) / 1000);

        System.out.println("Result matrix: ");
        for (int j = 0; j < Math.min(10, m_br); j++) {
            System.out.printf("%.2f ", phc[j]);
        }
        System.out.println();
    }
}
