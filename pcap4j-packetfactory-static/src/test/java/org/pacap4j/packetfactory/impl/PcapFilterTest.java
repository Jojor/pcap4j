package org.pacap4j.packetfactory.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapHandle.PcapDirection;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IcmpV4CommonPacket;
import org.pcap4j.packet.IcmpV4EchoPacket;
import org.pcap4j.packet.IcmpV4EchoReplyPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;

public class PcapFilterTest {
    private PcapHandle ph;

    private static final String UDP_TCP_ICMP_PCAP = "src/test/resources/org/pcap4j/packetfactory/static/udp_tcp_icmp.pcap";

    @Before
    public void setUp() throws Exception {
      ph = Pcaps.openOffline("src/test/resources/org/pcap4j/packetfactory/static/PcapHandleTest.pcap");
    }

    @After
    public void tearDown() throws Exception {
      if (ph != null) {
        ph.close();
      }
    }
    
    @Test
    public void testSetFilterIcmp() throws Exception {
        PcapHandle handle = null;
        try {
            handle = Pcaps.openOffline(UDP_TCP_ICMP_PCAP);
            handle.setFilter("icmp", BpfCompileMode.OPTIMIZE);
            int count = 0;
            try {
                while (true) {
                    Packet p = handle.getNextPacketEx();
                    assertNotNull(p.get(IcmpV4CommonPacket.class));
                    count++;
                }
            } catch (EOFException e) {
            }
            assertEquals(1, count);
        } finally {
            if (handle != null) {
                handle.close();
            }
        }
    }

    @Test
    public void testSetFilterUdp() throws Exception {
        PcapHandle handle = null;
        BpfProgram prog = null;
        try {
            handle = Pcaps.openOffline(UDP_TCP_ICMP_PCAP);
            prog = handle.compileFilter("udp", BpfCompileMode.OPTIMIZE, PcapHandle.PCAP_NETMASK_UNKNOWN);
            handle.setFilter(prog);
            int count = 0;
            try {
                while (true) {
                    Packet p = handle.getNextPacketEx();
                    assertNotNull(p.get(UdpPacket.class));
                    count++;
                }
            } catch (EOFException e) {
            }
            assertEquals(1, count);
        } finally {
            if (handle != null) {
                handle.close();
            }
            if (prog != null) {
                prog.free();
            }
        }
    }

    @Test
    public void testDirection() throws Exception {
      if (System.getenv("TRAVIS") != null) {
        // run only on Travis CI
        PcapHandle handle =
            new PcapHandle.Builder("any")
                .direction(PcapDirection.IN)
                .promiscuousMode(PromiscuousMode.PROMISCUOUS)
                .snaplen(65536)
                .timeoutMillis(10)
                .build();
        handle.setFilter("icmp", BpfCompileMode.OPTIMIZE);

        ProcessBuilder pb = new ProcessBuilder("/bin/ping", "www.google.com");
        Process process = pb.start();

        final List<Packet> packets = new ArrayList<Packet>();
        handle.loop(
            3,
            new PacketListener() {
              @Override
              public void gotPacket(Packet packet) {
                packets.add(packet);
              }
            });
        handle.close();
        process.destroy();

        assertEquals(3, packets.size());
        assertTrue(packets.get(0).contains(IcmpV4EchoReplyPacket.class));
        assertTrue(packets.get(1).contains(IcmpV4EchoReplyPacket.class));
        assertTrue(packets.get(2).contains(IcmpV4EchoReplyPacket.class));
      }
    }

    @Test
    public void testSetDirection() throws Exception {
      if (System.getenv("TRAVIS") != null) {
        // run only on Travis CI
        PcapNetworkInterface nif = Pcaps.getDevByName("any");
        PcapHandle handle = nif.openLive(65536, PromiscuousMode.PROMISCUOUS, 10);
        handle.setDirection(PcapDirection.OUT);
        handle.setFilter("icmp", BpfCompileMode.OPTIMIZE);

        ProcessBuilder pb = new ProcessBuilder("/bin/ping", "www.google.com");
        Process process = pb.start();

        final List<Packet> packets = new ArrayList<Packet>();
        handle.loop(
            3,
            new PacketListener() {
              @Override
              public void gotPacket(Packet packet) {
                packets.add(packet);
              }
            });
        handle.close();
        process.destroy();

        assertEquals(3, packets.size());
        assertTrue(packets.get(0).contains(IcmpV4EchoPacket.class));
        assertTrue(packets.get(1).contains(IcmpV4EchoPacket.class));
        assertTrue(packets.get(2).contains(IcmpV4EchoPacket.class));
      } else {
        try {
          ph.setDirection(PcapDirection.OUT);
          fail();
        } catch (PcapNativeException e) {
          assertTrue(e.getMessage().startsWith("Failed to set direction:"));
        }
      }
    }
}
