// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.network.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Local;
import javax.inject.Inject;

import org.apache.cloudstack.network.topology.NetworkTopology;
import org.cloud.network.router.deployment.RouterDeploymentDefinition;
import org.cloud.network.router.deployment.RouterDeploymentDefinitionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Network.State;
import com.cloud.network.NetworkModel;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.Site2SiteVpnGateway;
import com.cloud.network.VirtualRouterProvider.Type;
import com.cloud.network.VpnUser;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.Site2SiteVpnGatewayDao;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.network.router.VpcNetworkHelperImpl;
import com.cloud.network.router.VpcVirtualNetworkApplianceManager;
import com.cloud.network.vpc.NetworkACLItem;
import com.cloud.network.vpc.NetworkACLItemDao;
import com.cloud.network.vpc.NetworkACLItemVO;
import com.cloud.network.vpc.PrivateGateway;
import com.cloud.network.vpc.StaticRouteProfile;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcGateway;
import com.cloud.network.vpc.VpcManager;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.network.vpc.dao.VpcGatewayDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.utils.db.EntityManager;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineProfile;

@Local(value = { NetworkElement.class, FirewallServiceProvider.class, DhcpServiceProvider.class, UserDataServiceProvider.class, StaticNatServiceProvider.class,
        LoadBalancingServiceProvider.class, PortForwardingServiceProvider.class, IpDeployer.class, VpcProvider.class, Site2SiteVpnServiceProvider.class,
        NetworkACLServiceProvider.class })
public class VpcVirtualRouterElement extends VirtualRouterElement implements VpcProvider, Site2SiteVpnServiceProvider, NetworkACLServiceProvider {


    private static final Map<Service, Map<Capability, String>> capabilities = setCapabilities();

    @Inject
    VpcManager _vpcMgr;
    @Inject
    VpcVirtualNetworkApplianceManager _vpcRouterMgr;
    @Inject
    Site2SiteVpnGatewayDao _vpnGatewayDao;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    NetworkModel _ntwkModel;
    @Inject
    NetworkDao _networkDao;
    @Inject
    VpcGatewayDao _vpcGatewayDao;
    @Inject
    NetworkACLItemDao _networkACLItemDao;
    @Inject
    EntityManager _entityMgr;
    @Inject
    VirtualMachineManager _itMgr;
    @Inject
    IpAddressManager _ipAddrMgr;
    @Inject
    VpcDao _vpcDao;

    @Autowired
    @Qualifier("vpcNetworkHelper")
    private VpcNetworkHelperImpl _vpcNetWprkHelper;

    @Inject
    private RouterDeploymentDefinitionBuilder routerDeploymentDefinitionBuilder;

    @Override
    protected boolean canHandle(final Network network, final Service service) {
        final Long physicalNetworkId = _networkMdl.getPhysicalNetworkId(network);
        if (physicalNetworkId == null) {
            return false;
        }

        if (network.getVpcId() == null) {
            return false;
        }

        if (!_networkMdl.isProviderEnabledInPhysicalNetwork(physicalNetworkId, Network.Provider.VPCVirtualRouter.getName())) {
            return false;
        }

        if (service == null) {
            if (!_networkMdl.isProviderForNetwork(getProvider(), network.getId())) {
                logger.trace("Element " + getProvider().getName() + " is not a provider for the network " + network);
                return false;
            }
        } else {
            if (!_networkMdl.isProviderSupportServiceInNetwork(network.getId(), service, getProvider())) {
                logger.trace("Element " + getProvider().getName() + " doesn't support service " + service.getName() + " in the network " + network);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean implementVpc(final Vpc vpc, final DeployDestination dest, final ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException,
    InsufficientCapacityException {

        final Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>(1);
        params.put(VirtualMachineProfile.Param.ReProgramGuestNetworks, true);

        final RouterDeploymentDefinition routerDeploymentDefinition = routerDeploymentDefinitionBuilder.create().setVpc(vpc).setDeployDestination(dest)
                .setAccountOwner(_accountMgr.getAccount(vpc.getAccountId())).setParams(params).build();

        routerDeploymentDefinition.deployVirtualRouter();

        return true;
    }

    @Override
    public boolean shutdownVpc(final Vpc vpc, final ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        final List<DomainRouterVO> routers = _routerDao.listByVpcId(vpc.getId());
        if (routers == null || routers.isEmpty()) {
            return true;
        }

        boolean result = true;
        for (final DomainRouterVO router : routers) {
            result = result && _routerMgr.destroyRouter(router.getId(), context.getAccount(), context.getCaller().getId()) != null;
        }
        return result;
    }

    @Override
    public boolean implement(final Network network, final NetworkOffering offering, final DeployDestination dest, final ReservationContext context)
            throws ResourceUnavailableException, ConcurrentOperationException, InsufficientCapacityException {

        final Long vpcId = network.getVpcId();
        if (vpcId == null) {
            logger.trace("Network " + network + " is not associated with any VPC");
            return false;
        }

        final Vpc vpc = _vpcMgr.getActiveVpc(vpcId);
        if (vpc == null) {
            logger.warn("Unable to find Enabled VPC by id " + vpcId);
            return false;
        }

        final Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>(1);
        params.put(VirtualMachineProfile.Param.ReProgramGuestNetworks, true);

        final RouterDeploymentDefinition routerDeploymentDefinition = routerDeploymentDefinitionBuilder.create()
                .setGuestNetwork(network)
                .setVpc(vpc)
                .setDeployDestination(dest)
                .setAccountOwner(_accountMgr.getAccount(vpc.getAccountId()))
                .setParams(params)
                .build();

        final List<DomainRouterVO> routers = routerDeploymentDefinition.deployVirtualRouter();

        if (routers == null || routers.size() == 0) {
            throw new ResourceUnavailableException("Can't find at least one running router!", DataCenter.class, network.getDataCenterId());
        }

        configureGuestNetwork(network, routers);

        return true;
    }

    protected void configureGuestNetwork(final Network network, final List<DomainRouterVO> routers )
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {

        logger.info("Adding VPC routers to Guest Network: " + routers.size() + " to be added!");

        for (final DomainRouterVO router : routers) {
            if (!_networkMdl.isVmPartOfNetwork(router.getId(), network.getId())) {
                final Map<VirtualMachineProfile.Param, Object> paramsForRouter = new HashMap<VirtualMachineProfile.Param, Object>(1);
                if (network.getState() == State.Setup) {
                    paramsForRouter.put(VirtualMachineProfile.Param.ReProgramGuestNetworks, true);
                }
                if (!_vpcRouterMgr.addVpcRouterToGuestNetwork(router, network, paramsForRouter)) {
                    logger.error("Failed to add VPC router " + router + " to guest network " + network);
                } else {
                    logger.debug("Successfully added VPC router " + router + " to guest network " + network);
                }
            }
        }
    }

    @Override
    public boolean prepare(final Network network, final NicProfile nic, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException {

        final Long vpcId = network.getVpcId();
        if (vpcId == null) {
            logger.trace("Network " + network + " is not associated with any VPC");
            return false;
        }

        final Vpc vpc = _vpcMgr.getActiveVpc(vpcId);
        if (vpc == null) {
            logger.warn("Unable to find Enabled VPC by id " + vpcId);
            return false;
        }

        if (vm.getType() == VirtualMachine.Type.User) {
            final Map<VirtualMachineProfile.Param, Object> params = new HashMap<VirtualMachineProfile.Param, Object>(1);
            params.put(VirtualMachineProfile.Param.ReProgramGuestNetworks, true);

            final RouterDeploymentDefinition routerDeploymentDefinition = routerDeploymentDefinitionBuilder.create()
                    .setGuestNetwork(network)
                    .setVpc(vpc)
                    .setDeployDestination(dest)
                    .setAccountOwner(_accountMgr.getAccount(vpc.getAccountId()))
                    .setParams(params)
                    .build();

            final List<DomainRouterVO> routers = routerDeploymentDefinition.deployVirtualRouter();

            if (routers == null || routers.size() == 0) {
                throw new ResourceUnavailableException("Can't find at least one running router!", DataCenter.class, network.getDataCenterId());
            }

            configureGuestNetwork(network, routers);
        }

        return true;
    }

    @Override
    public boolean shutdown(final Network network, final ReservationContext context, final boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException {
        boolean success = true;
        final Long vpcId = network.getVpcId();
        if (vpcId == null) {
            logger.debug("Network " + network + " doesn't belong to any vpc, so skipping unplug nic part");
            return success;
        }

        final List<? extends VirtualRouter> routers = _routerDao.listByVpcId(vpcId);
        for (final VirtualRouter router : routers) {
            // 1) Check if router is already a part of the network
            if (!_networkMdl.isVmPartOfNetwork(router.getId(), network.getId())) {
                logger.debug("Router " + router + " is not a part the network " + network);
                continue;
            }
            // 2) Call unplugNics in the network service
            success = success && _vpcRouterMgr.removeVpcRouterFromGuestNetwork(router, network);
            if (!success) {
                logger.warn("Failed to unplug nic in network " + network + " for virtual router " + router);
            } else {
                logger.debug("Successfully unplugged nic in network " + network + " for virtual router " + router);
            }
        }

        return success;
    }

    @Override
    public boolean destroy(final Network config, final ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        boolean success = true;
        final Long vpcId = config.getVpcId();
        if (vpcId == null) {
            logger.debug("Network " + config + " doesn't belong to any vpc, so skipping unplug nic part");
            return success;
        }

        final List<? extends VirtualRouter> routers = _routerDao.listByVpcId(vpcId);
        for (final VirtualRouter router : routers) {
            // 1) Check if router is already a part of the network
            if (!_networkMdl.isVmPartOfNetwork(router.getId(), config.getId())) {
                logger.debug("Router " + router + " is not a part the network " + config);
                continue;
            }
            // 2) Call unplugNics in the network service
            success = success && _vpcRouterMgr.removeVpcRouterFromGuestNetwork(router, config);
            if (!success) {
                logger.warn("Failed to unplug nic in network " + config + " for virtual router " + router);
            } else {
                logger.debug("Successfully unplugged nic in network " + config + " for virtual router " + router);
            }
        }

        return success;
    }

    @Override
    public Provider getProvider() {
        return Provider.VPCVirtualRouter;
    }

    @Override
    protected List<DomainRouterVO> getRouters(final Network network, final DeployDestination dest) {

        //1st time it runs the domain router of the VM shall be returned
        List<DomainRouterVO> routers = super.getRouters(network, dest);
        if (routers.size() > 0) {
            return routers;
        }

        //For the 2nd time it returns the VPC routers.
        final Long vpcId = network.getVpcId();
        if (vpcId == null) {
            logger.error("Network " + network + " is not associated with any VPC");
            return routers;
        }

        final Vpc vpc = _vpcMgr.getActiveVpc(vpcId);
        if (vpc == null) {
            logger.warn("Unable to find Enabled VPC by id " + vpcId);
            return routers;
        }

        final RouterDeploymentDefinition routerDeploymentDefinition = routerDeploymentDefinitionBuilder.create()
                .setGuestNetwork(network)
                .setVpc(vpc)
                .setDeployDestination(dest)
                .setAccountOwner(_accountMgr.getAccount(vpc.getAccountId()))
                .build();

        try {
            routers = routerDeploymentDefinition.deployVirtualRouter();
        } catch (final ConcurrentOperationException e) {
            logger.error("Error occurred when loading routers from routerDeploymentDefinition.deployVirtualRouter()!", e);
        } catch (final InsufficientCapacityException e) {
            logger.error("Error occurred when loading routers from routerDeploymentDefinition.deployVirtualRouter()!", e);
        } catch (final ResourceUnavailableException e) {
            logger.error("Error occurred when loading routers from routerDeploymentDefinition.deployVirtualRouter()!", e);
        }

        return routers;
    }

    private static Map<Service, Map<Capability, String>> setCapabilities() {
        final Map<Service, Map<Capability, String>> capabilities = new HashMap<Service, Map<Capability, String>>();
        capabilities.putAll(VirtualRouterElement.capabilities);

        final Map<Capability, String> sourceNatCapabilities = new HashMap<Capability, String>();
        sourceNatCapabilities.putAll(capabilities.get(Service.SourceNat));
        // TODO This kind of logic is already placed in the DB
        sourceNatCapabilities.put(Capability.RedundantRouter, "true");
        capabilities.put(Service.SourceNat, sourceNatCapabilities);

        final Map<Capability, String> vpnCapabilities = new HashMap<Capability, String>();
        vpnCapabilities.putAll(capabilities.get(Service.Vpn));
        vpnCapabilities.put(Capability.VpnTypes, "s2svpn");
        capabilities.put(Service.Vpn, vpnCapabilities);

        // remove firewall capability
        capabilities.remove(Service.Firewall);

        // add network ACL capability
        final Map<Capability, String> networkACLCapabilities = new HashMap<Capability, String>();
        networkACLCapabilities.put(Capability.SupportedProtocols, "tcp,udp,icmp");
        capabilities.put(Service.NetworkACL, networkACLCapabilities);

        return capabilities;
    }

    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean createPrivateGateway(final PrivateGateway gateway) throws ConcurrentOperationException, ResourceUnavailableException {
        if (gateway.getType() != VpcGateway.Type.Private) {
            logger.warn("Type of vpc gateway is not " + VpcGateway.Type.Private);
            return false;
        }

        final List<DomainRouterVO> routers = _vpcRouterMgr.getVpcRouters(gateway.getVpcId());
        if (routers == null || routers.isEmpty()) {
            logger.debug(getName() + " element doesn't need to create Private gateway on the backend; VPC virtual " + "router doesn't exist in the vpc id=" + gateway.getVpcId());
            return true;
        }

        logger.info("Adding VPC routers to Guest Network: " + routers.size() + " to be added!");

        final DataCenterVO dcVO = _dcDao.findById(gateway.getZoneId());
        final NetworkTopology networkTopology = networkTopologyContext.retrieveNetworkTopology(dcVO);

        for (final DomainRouterVO domainRouterVO : routers) {
            if (networkTopology.setupPrivateGateway(gateway, domainRouterVO)) {
                try {
                    final List<NetworkACLItemVO> rules = _networkACLItemDao.listByACL(gateway.getNetworkACLId());
                    if (!applyACLItemsToPrivateGw(gateway, rules)) {
                        logger.debug("Failed to apply network acl id  " + gateway.getNetworkACLId() + "  on gateway ");
                        return false;
                    }
                } catch (final Exception ex) {
                    logger.debug("Failed to apply network acl id  " + gateway.getNetworkACLId() + "  on gateway ");
                    return false;
                }
            } else {
                logger.debug("Failed to setup private gateway  " + gateway);
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean deletePrivateGateway(final PrivateGateway gateway) throws ConcurrentOperationException, ResourceUnavailableException {
        if (gateway.getType() != VpcGateway.Type.Private) {
            logger.warn("Type of vpc gateway is not " + VpcGateway.Type.Private);
            return false;
        }

        final List<DomainRouterVO> routers = _vpcRouterMgr.getVpcRouters(gateway.getVpcId());
        if (routers == null || routers.isEmpty()) {
            logger.debug(getName() + " element doesn't need to delete Private gateway on the backend; VPC virtual " + "router doesn't exist in the vpc id=" + gateway.getVpcId());
            return true;
        }

        logger.info("Adding VPC routers to Guest Network: " + routers.size() + " to be added!");

        int result = 0;
        for (final DomainRouterVO domainRouterVO : routers) {
            if (_vpcRouterMgr.destroyPrivateGateway(gateway, domainRouterVO)) {
                result++;
            }
        }

        return result > 0 ? true : false;
    }

    @Override
    public boolean applyIps(final Network network, final List<? extends PublicIpAddress> ipAddress, final Set<Service> services) throws ResourceUnavailableException {
        boolean canHandle = true;
        for (final Service service : services) {
            if (!canHandle(network, service)) {
                canHandle = false;
                break;
            }
        }
        if (canHandle) {
            final List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            if (routers == null || routers.isEmpty()) {
                logger.debug(getName() + " element doesn't need to associate ip addresses on the backend; VPC virtual " + "router doesn't exist in the network "
                        + network.getId());
                return true;
            }

            final DataCenterVO dcVO = _dcDao.findById(network.getDataCenterId());
            final NetworkTopology networkTopology = networkTopologyContext.retrieveNetworkTopology(dcVO);

            return networkTopology.associatePublicIP(network, ipAddress, routers);
        } else {
            return false;
        }
    }

    @Override
    public boolean applyNetworkACLs(final Network network, final List<? extends NetworkACLItem> rules) throws ResourceUnavailableException {
        if (canHandle(network, Service.NetworkACL)) {
            final List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.VIRTUAL_ROUTER);
            if (routers == null || routers.isEmpty()) {
                logger.debug("Virtual router elemnt doesn't need to apply firewall rules on the backend; virtual " + "router doesn't exist in the network " + network.getId());
                return true;
            }

            final DataCenterVO dcVO = _dcDao.findById(network.getDataCenterId());
            final NetworkTopology networkTopology = networkTopologyContext.retrieveNetworkTopology(dcVO);

            try {
                if (!networkTopology.applyNetworkACLs(network, rules, routers, false)) {
                    return false;
                } else {
                    return true;
                }
            } catch (final Exception ex) {
                logger.debug("Failed to apply network acl in network " + network.getId());
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    protected Type getVirtualRouterProvider() {
        return Type.VPCVirtualRouter;
    }

    @Override
    public boolean applyStaticRoutes(final Vpc vpc, final List<StaticRouteProfile> routes) throws ResourceUnavailableException {
        final List<DomainRouterVO> routers = _routerDao.listByVpcId(vpc.getId());
        if (routers == null || routers.isEmpty()) {
            logger.debug("Virtual router elemnt doesn't need to static routes on the backend; virtual " + "router doesn't exist in the vpc " + vpc);
            return true;
        }

        final DataCenterVO dcVO = _dcDao.findById(vpc.getZoneId());
        final NetworkTopology networkTopology = networkTopologyContext.retrieveNetworkTopology(dcVO);

        if (!networkTopology.applyStaticRoutes(routes, routers)) {
            throw new CloudRuntimeException("Failed to apply static routes in vpc " + vpc);
        } else {
            logger.debug("Applied static routes on vpc " + vpc);
            return true;
        }
    }

    @Override
    public boolean applyACLItemsToPrivateGw(final PrivateGateway gateway, final List<? extends NetworkACLItem> rules) throws ResourceUnavailableException {
        final Network network = _networkDao.findById(gateway.getNetworkId());
        final boolean isPrivateGateway = true;

        final List<DomainRouterVO> routers = _vpcRouterMgr.getVpcRouters(gateway.getVpcId());
        if (routers == null || routers.isEmpty()) {
            logger.debug("Virtual router element doesn't need to apply network acl rules on the backend; virtual " + "router doesn't exist in the network " + network.getId());
            return true;
        }

        final DataCenterVO dcVO = _dcDao.findById(network.getDataCenterId());
        final NetworkTopology networkTopology = networkTopologyContext.retrieveNetworkTopology(dcVO);

        if (!networkTopology.applyNetworkACLs(network, rules, routers, isPrivateGateway)) {
            throw new CloudRuntimeException("Failed to apply network acl in network " + network.getId());
        } else {
            return true;
        }
    }

    @Override
    public boolean startSite2SiteVpn(final Site2SiteVpnConnection conn) throws ResourceUnavailableException {
        final Site2SiteVpnGateway vpnGw = _vpnGatewayDao.findById(conn.getVpnGatewayId());
        final IpAddress ip = _ipAddressDao.findById(vpnGw.getAddrId());

        final Map<Capability, String> vpnCapabilities = capabilities.get(Service.Vpn);
        if (!vpnCapabilities.get(Capability.VpnTypes).contains("s2svpn")) {
            logger.error("try to start site 2 site vpn on unsupported network element?");
            return false;
        }

        final Long vpcId = ip.getVpcId();
        final Vpc vpc = _entityMgr.findById(Vpc.class, vpcId);

        if (!_ntwkModel.isProviderEnabledInZone(vpc.getZoneId(), Provider.VPCVirtualRouter.getName())) {
            throw new ResourceUnavailableException("VPC provider is not enabled in zone " + vpc.getZoneId(), DataCenter.class, vpc.getZoneId());
        }

        final List<DomainRouterVO> routers = _vpcRouterMgr.getVpcRouters(ip.getVpcId());
        if (routers == null) {
            throw new ResourceUnavailableException("Cannot enable site-to-site VPN on the backend; virtual router doesn't exist in the vpc " + ip.getVpcId(), DataCenter.class,
                    vpc.getZoneId());
        }

        boolean result = true;
        for (final DomainRouterVO domainRouterVO : routers) {
            result = result && _vpcRouterMgr.startSite2SiteVpn(conn, domainRouterVO);
        }
        return result;
    }

    @Override
    public boolean stopSite2SiteVpn(final Site2SiteVpnConnection conn) throws ResourceUnavailableException {
        final Site2SiteVpnGateway vpnGw = _vpnGatewayDao.findById(conn.getVpnGatewayId());
        final IpAddress ip = _ipAddressDao.findById(vpnGw.getAddrId());

        final Map<Capability, String> vpnCapabilities = capabilities.get(Service.Vpn);
        if (!vpnCapabilities.get(Capability.VpnTypes).contains("s2svpn")) {
            logger.error("try to stop site 2 site vpn on unsupported network element?");
            return false;
        }

        final Long vpcId = ip.getVpcId();
        final Vpc vpc = _entityMgr.findById(Vpc.class, vpcId);

        if (!_ntwkModel.isProviderEnabledInZone(vpc.getZoneId(), Provider.VPCVirtualRouter.getName())) {
            throw new ResourceUnavailableException("VPC provider is not enabled in zone " + vpc.getZoneId(), DataCenter.class, vpc.getZoneId());
        }

        final List<DomainRouterVO> routers = _vpcRouterMgr.getVpcRouters(ip.getVpcId());
        if (routers == null) {
            throw new ResourceUnavailableException("Cannot enable site-to-site VPN on the backend; virtual router doesn't exist in the vpc " + ip.getVpcId(), DataCenter.class,
                    vpc.getZoneId());
        }

        boolean result = true;
        for (final DomainRouterVO domainRouterVO : routers) {
            result = result && _vpcRouterMgr.stopSite2SiteVpn(conn, domainRouterVO);
        }

        return result;
    }

    @Override
    public String[] applyVpnUsers(final RemoteAccessVpn vpn, final List<? extends VpnUser> users) throws ResourceUnavailableException {
        if (vpn.getVpcId() == null) {
            return null;
        }

        final List<DomainRouterVO> routers = _vpcRouterMgr.getVpcRouters(vpn.getVpcId());
        if (routers == null) {
            logger.debug("Cannot apply vpn users on the backend; virtual router doesn't exist in the network " + vpn.getVpcId());
            return null;
        }

        final Vpc vpc = _entityMgr.findById(Vpc.class, vpn.getVpcId());
        final DataCenterVO dcVO = _dcDao.findById(vpc.getZoneId());
        final NetworkTopology networkTopology = networkTopologyContext.retrieveNetworkTopology(dcVO);

        String[] result = null;
        for (final DomainRouterVO domainRouterVO : routers) {
            result = networkTopology.applyVpnUsers(vpn, users, domainRouterVO);
        }
        return result;
    }

    @Override
    public boolean startVpn(final RemoteAccessVpn vpn) throws ResourceUnavailableException {
        if (vpn.getVpcId() == null) {
            return false;
        }

        final List<DomainRouterVO> routers = _vpcRouterMgr.getVpcRouters(vpn.getVpcId());
        if (routers == null) {
            logger.debug("Cannot apply vpn users on the backend; virtual router doesn't exist in the network " + vpn.getVpcId());
            return false;
        }

        boolean result = true;
        for (final DomainRouterVO domainRouterVO : routers) {
            result = result && _vpcRouterMgr.startRemoteAccessVpn(vpn, domainRouterVO);
        }
        return result;
    }

    @Override
    public boolean stopVpn(final RemoteAccessVpn vpn) throws ResourceUnavailableException {
        if (vpn.getVpcId() == null) {
            return false;
        }

        final List<DomainRouterVO> routers = _vpcRouterMgr.getVpcRouters(vpn.getVpcId());
        if (routers == null) {
            logger.debug("Cannot apply vpn users on the backend; virtual router doesn't exist in the network " + vpn.getVpcId());
            return false;
        }

        boolean result = true;
        for (final DomainRouterVO domainRouterVO : routers) {
            result = result && _vpcRouterMgr.stopRemoteAccessVpn(vpn, domainRouterVO);
        }
        return result;
    }
}