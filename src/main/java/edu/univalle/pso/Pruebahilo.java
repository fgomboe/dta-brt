package edu.univalle.pso;

public class Pruebahilo
{
    private static Multihilo[] fitnessValueList = new Multihilo[20];
    private static Location location;

    public static void main(String args[]) {
        for (int i = 0; i < 20; i++) {
            fitnessValueList[i] = new Multihilo(i, location); // The class containing the function to be launched
            fitnessValueList[i].start();

        }

        // Waits for last instance to end
        try {
            System.out.println("Waiting for threads to finish.");
            for (int i = 0; i < 20; i++) {
                fitnessValueList[i].hilo.join();
            }

        }
        catch (InterruptedException e) {
            System.out.println("Main thread Interrupted");
        }

        for (int i = 0; i < 20; i++) {
            System.out.println(Multihilo.Resultados[i]);
        }
    }
}