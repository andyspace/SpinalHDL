package spinal.lib.bus.regif.BusIfAdapter

import spinal.core._
import spinal.lib.bus.localbus.MinBus
import spinal.lib.bus.misc.SizeMapping
import spinal.lib.bus.regif._

case class MinBusInterface(bus: MinBus, sizeMap: SizeMapping, regPre: String = "")(implicit moduleName: ClassName) extends BusIf{
  override val busDataWidth: Int = bus.c.dw
  override val busAddrWidth: Int = bus.c.aw
  override val withStrb: Boolean = bus.c.withStrb
  override def getModuleName = moduleName.name

  val bus_rderr: Bool = Bool()
  val bus_rdata: Bits  = Bits(busDataWidth bits)
  val reg_rderr: Bool = Reg(Bool(), init = False)
  val reg_rdata: Bits = Reg(Bits(busDataWidth bits), init = defualtReadBits)

  val wstrb: Bits  = withStrb generate(Bits(strbWidth bit))
  val wmask: Bits  = withStrb generate(Bits(busDataWidth bit))
  val wmaskn: Bits = withStrb generate(Bits(busDataWidth bit))
  withStrb generate(wstrb := bus.strb)

  val askWrite  = bus.ce && bus.wr
  val askRead   = bus.ce && !bus.wr
  val doWrite   = (askWrite && bus.rdy).allowPruning()
  val doRead    = (askRead  && bus.rdy).allowPruning()
  val writeData = bus.wdat
  override val cg_en: Bool = bus.ce
  override val NS: Bool = bus.prot(1)

  bus.rdy := True
  bus.rvld := RegNext(askRead, False)
  bus.rdat := bus_rdata

  initStrbMasks()

  override def readAddress()  = if(withStrb) (bus.addr.drop(underbitWidth) ## B(0, underbitWidth bit)).asUInt else bus.addr
  override def writeAddress() = bus.addr

  override def readHalt()  = bus.rdy := False
  override def writeHalt() = bus.rdy := False
}
