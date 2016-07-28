package edu.univalle.pso;

class Multihilo implements Runnable, PSOConstants
{
    Thread hilo;

    private int resultado;
    int numero;
    private Location location_swarm;

    public static int[] Resultados = new int[SWARM_SIZE];
    public static double[] Resultados2 = new double[SWARM_SIZE];

    // Here is the function to be executed
    public int operar(int d) {
        resultado = d * 1000;
        return resultado;
    }

    Multihilo(int n_hilo, Location location) {
        numero = n_hilo;
        location_swarm = location;

    }

    public void run() {

        // Here is the function to be executed
        Resultados2[numero] = ProblemSet.evaluate(location_swarm);
        // Multihilo.Resultados[numero] = operar(numero);

        // use this try-catch if you are using Thread.sleep
        // try {
        // for (int i = 4; i > 0; i--) {
        // System.out.println("Hilo: " + Integer.toString(numero) + ", " + i);
        // // This call the function to be executed
        // // Multihilo.Resultados[numero] = operar(numero);
        // //Multihilo.Resultados2[numero] = ProblemSet.evaluate(location2);
        // Thread.sleep(50);
        //
        // }
        // }
        // catch (InterruptedException e) {
        //
        // }
        // System.out.println("Termina " + numero);
    }

    public void start() {

        if (hilo == null) {

            hilo = new Thread(this);
            hilo.start();
            // System.out.println("Iniciando " + numero);

        }
    }
}
