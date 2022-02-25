package uk.gov.dwp.jsa.validation.service.repositories;

import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryBuilderFactory;
import org.hibernate.boot.spi.SessionFactoryBuilderImplementor;
import org.hibernate.type.UUIDCharType;


/**
 * As we're using the type {@link java.util.UUID} as our keys in the entities, Hibernate maps this by default to a
 * binary type. This prevents INSERT commands working where the UUID is passed in as a string.
 *
 * One method of bypassing this default association is to annotate the entity fields in question with:
 * <pre>
 * <code>
 *
 * {@literal @}Id
 * {@literal @}GeneratedValue(generator = "UUID")
 * {@literal @}GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
 * {@literal @}Column(name = "id", updatable = false, nullable = false, unique = true)
 * {@literal @}Type(type = "uuid-char")
 *  private UUID bookingStatusId;
 * </code>
 * </pre>
 *
 * Avoided this however as not fully sure as to what effect it may have on live (if any) so the type only exists in test
 * code. Also see file {@code src/test/resources/META-INF/services/org.hibernate.boot.spi.SessionFactoryBuilderFactory}
 * for where this class is registered. This then allows our SQL files to run in database integration tests with strings
 * as the UUIDs.
 */
public class HibernateDataTypeOverride implements SessionFactoryBuilderFactory {
    @Override
    public SessionFactoryBuilder getSessionFactoryBuilder(final MetadataImplementor metadataImplementor,
                                                          final SessionFactoryBuilderImplementor sessionFactoryBuilderImplementor) {
        metadataImplementor.getTypeConfiguration().getBasicTypeRegistry().unregister("java.util.UUID");
        metadataImplementor.getTypeConfiguration().getBasicTypeRegistry().register(UUIDCharType.INSTANCE, new String[]{"java.util.UUID"});
        return sessionFactoryBuilderImplementor;
    }
}
