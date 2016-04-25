package pl.setblack.exp.galakpizza.system;

import pl.setblack.airomem.core.Storable;
import pl.setblack.exp.galakpizza.api.GalakPizzaService;
import pl.setblack.exp.galakpizza.domain.Order;
import pl.setblack.exp.galakpizza.domain.Size;
import pl.setblack.exp.galakpizza.domain.Variant;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


public class GalakPizzaCore implements GalakPizzaService, Serializable, Storable<GalakPizzaCore> {
    private final Map<String, PlanetOrders> orders = new HashMap<>();

    private long orderSequence = 1;

    private PriorityQueue<PlanetOrders> bestPlanets = new PriorityQueue<>(256);

    private AtomicLong ordersTotal = new AtomicLong(0);

    public long placeOrder(String planet, Variant variant, Size size) {
        final long id = orderSequence++;
        final Order order = new Order(id, planet, variant, size);
        assignOrderToPlanet(order);
        ordersTotal.incrementAndGet();
        return id;
    }

    public List<Order> takeOrdersFromBestPlanet() {
        final Optional<PlanetOrders> planetOpt = takeBestPlanet();
        if (planetOpt.isPresent()) {
            final PlanetOrders planet = planetOpt.get();
            List<Order> orders = planet.takeOrders();
            ordersTotal.addAndGet(-orders.size());
            return orders;
        }
        return Collections.EMPTY_LIST;
    }

    private Optional<PlanetOrders> takeBestPlanet() {

        PlanetOrders planet = this.bestPlanets.poll();
        while (planet != null && planet.isEmptied()) {
            planet = this.bestPlanets.poll();
        }
        return Optional.ofNullable(planet);

    }

    @Override
    public long countStandingOrders() {
        return this.ordersTotal.get();
    }

    private void assignOrderToPlanet(Order order) {
        final PlanetOrders po = orders.computeIfAbsent(order.planet,
                planetName -> new PlanetOrders(planetName));
        po.assignOrder(order);
        this.bestPlanets.offer(po);
    }

    @Override
    public GalakPizzaCore getImmutable() {
        return this;
    }
}
