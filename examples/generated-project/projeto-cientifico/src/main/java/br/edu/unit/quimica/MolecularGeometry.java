package br.edu.unit.quimica;

/**
 * Primeira classe cientifica do projeto.
 *
 * <p>Exemplo de geometria molecular: distancia entre duas posicoes atomicas em coordenadas
 * cartesianas. Substitua ou amplie com os calculos do seu projeto.
 */
public final class MolecularGeometry {

  private MolecularGeometry() {
  }

  /**
   * Calcula a distancia euclidiana entre dois pontos no espaco 3D.
   *
   * @param a coordenadas (x, y, z) do primeiro atomo
   * @param b coordenadas (x, y, z) do segundo atomo
   * @return distancia, na mesma unidade das coordenadas (por exemplo, angstrom)
   * @throws IllegalArgumentException se algum ponto for nulo ou nao tiver 3 coordenadas
   */
  public static double distance(double[] a, double[] b) {
    if (a == null || b == null || a.length != 3 || b.length != 3) {
      throw new IllegalArgumentException("Cada ponto deve ter exatamente 3 coordenadas");
    }
    double sum = 0.0;
    for (int i = 0; i < 3; i++) {
      double delta = a[i] - b[i];
      sum += delta * delta;
    }
    return java.lang.Math.sqrt(sum);
  }
}
