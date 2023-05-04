package org.openmrs.module.disa.api.sync;

import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.module.disa.LabResult;
import org.openmrs.module.disa.LabResultStatus;
import org.openmrs.module.disa.api.exception.DisaModuleAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Adds the provider to the sync context.
 */
@Component
public class ProviderLookup extends BaseLabResultHandler {

    public static final String PROVIDER_KEY = "PROVIDER";

    private UserService userService;

    private ProviderService providerService;

    @Autowired
    public ProviderLookup(UserService userService, ProviderService providerService) {
        this.userService = userService;
        this.providerService = providerService;
    }

    @Override
    public LabResultStatus handle(LabResult labResult) {

        if (labResult.isPending()) {

            User user = userService.getUserByUsername("generic.provider");
            if (user == null) {
                user = userService.getUserByUsername("provedor.desconhecido");
            }

            if (user == null) {
                throw new DisaModuleAPIException(
                        "O erro O provedor generic.provider ou provedor.desconhecido nao foi encontrado no OpenMRS.");
            }

            Provider provider = providerService.getProvidersByPerson(user.getPerson()).iterator().next();
            getSyncContext().put(PROVIDER_KEY, provider);
        }

        return super.handle(labResult);
    }
}
