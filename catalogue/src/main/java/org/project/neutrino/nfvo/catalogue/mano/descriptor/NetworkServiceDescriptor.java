/*#############################################################################
 # Copyright (c) 2015.                                                        #
 #                                                                            #
 # This file is part of the OpenSDNCore project.                              #
 #############################################################################*/

package org.project.neutrino.nfvo.catalogue.mano.descriptor;

import org.project.neutrino.nfvo.catalogue.mano.common.NFVEntityDescriptor;
import org.project.neutrino.nfvo.catalogue.mano.common.Security;
import org.project.neutrino.nfvo.catalogue.mano.common.VNFDependency;

import javax.persistence.*;
import java.util.List;

/**
 * Created by lto on 05/02/15.
 *
 * Based on ETSI GS NFV-MAN 001 V1.1.1 (2014-12)
 */
@Entity
public class NetworkServiceDescriptor extends NFVEntityDescriptor {

    /**
     * VNF which is part of the Network Service, see clause 6.3.1. This element is required, for example, when the Network Service is being built top-down or instantiating the member VNFs as well.
     * */
    @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private List<VirtualNetworkFunctionDescriptor> vnfd;
    /**
     * Describe dependencies between VNF. Defined in terms of
     * source and target VNF i.e. target VNF "depends on" source
     * VNF. In other words a source VNF shall exist and connect to
     * the service before target VNF can be initiated/deployed and
     * connected. This element would be used, for example, to define
     * the sequence in which various numbered network nodes and
     * links within a VNF FG should be instantiated by the NFV
     * Orchestrator.*/
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VNFDependency> vnf_dependency;
    /*See PhysicalNetworkFunctionDescriptor class for description*/
    @OneToMany(cascade = CascadeType.ALL)
    private List<PhysicalNetworkFunctionDescriptor> pnfd;
    /*
    * This is a signature of nsd to prevent tampering. The particular hash algorithm used to compute the signature, together with the
    * corresponding cryptographic certificate to validate the signature should also be included.
    * Not mandatory from NFV.
    * TODO could also be called Security and used for all the objects that need it.
    * */
    @OneToOne(cascade = CascadeType.ALL)
    private Security nsd_security;

    public NetworkServiceDescriptor() {
    }


    public List<VirtualNetworkFunctionDescriptor> getVnfd() {
		return vnfd;
	}


	public void setVnfd(List<VirtualNetworkFunctionDescriptor> vnfd) {
		this.vnfd = vnfd;
	}


	public List<VNFDependency> getVnf_dependency() {
		return vnf_dependency;
	}


	public void setVnf_dependency(List<VNFDependency> vnf_dependency) {
		this.vnf_dependency = vnf_dependency;
	}


	public List<PhysicalNetworkFunctionDescriptor> getPnfd() {
		return pnfd;
	}


	public void setPnfd(List<PhysicalNetworkFunctionDescriptor> pnfd) {
		this.pnfd = pnfd;
	}


	public Security getNsd_security() {
		return nsd_security;
	}


	public void setNsd_security(Security nsd_security) {
		this.nsd_security = nsd_security;
	}


	@Override
	public String toString() {
		return "NetworkServiceDescriptor [vnfd=" + vnfd + ", vnf_dependency="
				+ vnf_dependency + ", pnfd=" + pnfd + ", nsd_security="
				+ nsd_security + ", Id=" + Id + ", hb_version=" + hb_version
				+ ", vendor=" + vendor + ", version=" + version + ", vnffgd="
				+ vnffgd + ", vld=" + vld + ", lifecycle_event="
				+ lifecycle_event + ", monitoring_parameter="
				+ monitoring_parameter + ", service_deployment_flavour="
				+ service_deployment_flavour + ", auto_scale_policy="
				+ auto_scale_policy + ", connection_point=" + connection_point
				+ "]";
	}
}