/*_##########################################################################
  _##
  _##  Copyright (C) 2016  Pcap4J.org
  _##
  _##########################################################################
*/

package org.pcap4j.packet.factory.impl;

import org.pcap4j.packet.namednumber.SctpPort;

/**
 * @author Jeff Myers
 * @since pcap4j 1.6.6
 */
public final class StaticSctpPortPacketFactory extends AbstractStaticPacketFactory<SctpPort> {

  private static final StaticSctpPortPacketFactory INSTANCE = new StaticSctpPortPacketFactory();

  private StaticSctpPortPacketFactory() {
    //    instantiaters.put(
    //      SctpPort.HTTP, new PacketInstantiater() {
    //        @Override
    //        public Packet newInstance(
    //          byte[] rawData, int offset, int length
    //        ) throws IllegalRawDataException {
    //          return HttpPacket.newPacket(rawData, offset, length);
    //        }
    //        @Override
    //        public Class<HttpPacket> getTargetClass() {
    //          return HttpPacket.class;
    //        }
    //      }
    //    );
  };

  /** @return the singleton instance of StaticSctpPortPacketFactory. */
  public static StaticSctpPortPacketFactory getInstance() {
    return INSTANCE;
  }
}
