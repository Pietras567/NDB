package NBD;

import com.datastax.oss.driver.api.core.addresstranslation.AddressTranslator;
import com.datastax.oss.driver.api.core.context.DriverContext;

import java.net.InetSocketAddress;

/**
 * A custom implementation of the {@link AddressTranslator} interface that translates
 * specific IP addresses to associated {@link InetSocketAddress} instances for use in
 * a car rental application. This implementation provides specific mappings to predefined
 * hostnames and ports based on IP addresses.
 *
 * The {@code CarRentalAddressTranslator} is designed to be used in a context where
 * Cassandra database nodes are identified by specific IPs, and hostnames need to be
 * resolved dynamically based on these mappings.
 *
 * The translation logic maps the following IP addresses to respective hostnames and ports:
 * - 172.18.0.2 -> cassandra1:9042
 * - 172.18.0.3 -> cassandra2:9043
 * - 172.18.0.4 -> cassandra3:9044
 *
 * For any IP address outside of the predefined mappings, a {@link RuntimeException} is thrown.
 *
 * This class must also implement the {@code close()} method to adhere to the requirements
 * of the {@link AddressTranslator} interface, though it performs no specific action in this case.
 */
public class CarRentalAddressTranslator implements AddressTranslator {
    public CarRentalAddressTranslator(DriverContext driverContext) {

    }

    /**
     * Translates the given {@link InetSocketAddress} to a different {@link InetSocketAddress}
     * based on predefined mappings of IP addresses to hostnames and ports.
     *
     * @param address the input {@link InetSocketAddress} to be translated, containing the original IP address and port.
     * @return a new {@link InetSocketAddress} with the translated hostname and port if the given IP matches a predefined mapping.
     * @throws RuntimeException if the IP address does not match any predefined mappings.
     */
    public InetSocketAddress translate(InetSocketAddress address) {
        String hostAddress = address.getAddress().getHostAddress();
        String hostName = address.getHostName();
        return switch (hostAddress) {
            case "172.18.0.2" -> new InetSocketAddress("cassandra1", 9042);
            case "172.18.0.3" -> new InetSocketAddress("cassandra2", 9043);
            case "172.18.0.4" -> new InetSocketAddress("cassandra3", 9044);
            default -> throw new RuntimeException("Unknown host: " + hostName);
        };
    }

    @Override
    public void close() {}
}
