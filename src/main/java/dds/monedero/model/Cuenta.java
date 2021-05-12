package dds.monedero.model;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cuenta {

  private double saldo;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  /*public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }*/

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }

    if (movimientos.stream().filter(Movimiento::isEsDeposito).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }

    this.agregarMovimiento(LocalDate.now(), cuanto, true);
  }

  public void sacar(double cuanto) {
    if (cuanto <= 0) {
      throw new MontoNegativoException(cuanto + ": el monto a ingresar debe ser un valor positivo");
    }
    if (getSaldo() - cuanto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (cuanto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
    this.agregarMovimiento(LocalDate.now(), cuanto, false);


  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto, esDeposito);
    movimientos.add(movimiento);
    this.setSaldo(this.calcularNuevoSaldo(movimiento));
  }

  public double calcularNuevoSaldo(Movimiento movimiento){
    if(movimiento.isEsDeposito())
      return this.getSaldo() + movimiento.getMonto();
    else
      return this.getSaldo() - movimiento.getMonto();
  }


  public double getMontoExtraidoA(LocalDate fecha) {
    return movimientos.stream()
        .filter(movimiento -> !movimiento.fueExtraido(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }


  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
