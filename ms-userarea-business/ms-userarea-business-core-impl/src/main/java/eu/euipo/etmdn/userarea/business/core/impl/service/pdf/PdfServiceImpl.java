/*
 * $Id:: PdfServiceImpl.java 2021/04/02 01:36 tantonop
 *
 *        . * .
 *      * RRRR  *   Copyright (c) 2012-2021 EUIPO: European Intelectual
 *     .  RR  R  .  Property Organization (trademarks and designs).
 *     *  RRR    *
 *      . RR RR .   ALL RIGHTS RESERVED
 *       *. _ .*
 *
 *  The use and distribution of this software is under the restrictions exposed in 'license.txt'
 *
 */

package eu.euipo.etmdn.userarea.business.core.impl.service.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import eu.euipo.etmdn.userarea.business.core.api.service.ApplicationService;
import eu.euipo.etmdn.userarea.business.core.api.service.PaymentService;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfGenerator;
import eu.euipo.etmdn.userarea.business.core.api.service.PdfService;
import eu.euipo.etmdn.userarea.business.core.impl.domain.CorrespondencePdfData;
import eu.euipo.etmdn.userarea.business.core.impl.domain.InvoicePdfData;
import eu.euipo.etmdn.userarea.business.core.impl.factory.PdfGeneratorFactory;
import eu.euipo.etmdn.userarea.business.core.impl.mapper.CorrespondencePdfDataMapper;
import eu.euipo.etmdn.userarea.business.core.impl.mapper.InvoicePdfDataMapper;
import eu.euipo.etmdn.userarea.common.business.config.IpoConfiguration;
import eu.euipo.etmdn.userarea.common.business.config.PdfTemplateConfiguration;
import eu.euipo.etmdn.userarea.common.business.correspondence.MessageAttachmentService;
import eu.euipo.etmdn.userarea.common.business.helper.PdfTemplateHelper;
import eu.euipo.etmdn.userarea.common.business.service.AccountService;
import eu.euipo.etmdn.userarea.common.domain.DomainAccount;
import eu.euipo.etmdn.userarea.common.domain.FileInfo;
import eu.euipo.etmdn.userarea.common.domain.PdfTemplate;
import eu.euipo.etmdn.userarea.common.domain.correspondence.Attachment;
import eu.euipo.etmdn.userarea.common.domain.correspondence.DraftAttachment;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageMediaTypes;
import eu.euipo.etmdn.userarea.common.domain.correspondence.MessageStatus;
import eu.euipo.etmdn.userarea.common.domain.document.FileResponse;
import eu.euipo.etmdn.userarea.common.domain.exception.InvalidDownloadAttachmentUserException;
import eu.euipo.etmdn.userarea.common.domain.exception.UserAreaException;
import eu.euipo.etmdn.userarea.common.persistence.document.DocumentClient;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.DraftEntity;
import eu.euipo.etmdn.userarea.common.persistence.entity.correspondence.MessageEntity;
import eu.euipo.etmdn.userarea.common.persistence.repository.correspondence.DraftRepository;
import eu.euipo.etmdn.userarea.common.persistence.repository.correspondence.MessageRepository;
import eu.euipo.etmdn.userarea.domain.ApplicationType;
import eu.euipo.etmdn.userarea.domain.application.ApplicationRequest;
import eu.euipo.etmdn.userarea.domain.payment.InvoiceDetails;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static eu.euipo.etmdn.userarea.common.business.utils.AccountUtils.validateAccountAccess;

/**
 * The PdfServiceImpl class.
 */

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Service
public class PdfServiceImpl implements PdfService {

    @Autowired
    private PdfTemplateConfiguration pdfTemplateConfiguration;
    @Autowired
    private PdfTemplateHelper pdfTemplateHelper;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private DraftRepository draftRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private IpoConfiguration ipoConfiguration;
    @Autowired
    private MessageAttachmentService messageAttachmentService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private DocumentClient documentClient;

    @Value("${userarea.resourcesBaseUri}")
    private String resourcesBaseUri;

    /**
     * Generate pdf file.
     *
     * @param applicationRequest the applicationRequest
     * @param roles              the logged in user's set of roles
     * @return {@link FileInfo} the generated pdf file information
     */
    @Override
    public FileInfo generatePdf(ApplicationRequest applicationRequest, Set<String> roles) {
        PdfGenerator pdfGenerator = PdfGeneratorFactory.getPdfDataGenerator(ApplicationType.getApplicationType(applicationRequest.getApplicationType()),
                applicationService.getApplications(applicationRequest, roles), roles, applicationRequest.getIsDraft());
        return toPdf(pdfGenerator);
    }

    /**
     * generates an incoming message pdf
     *
     * @param username  the authenticated user
     * @param isDraft   if its a draft request
     * @param messageId the message id
     * @return {@link FileInfo} the generated pdf file information
     */
    @Override
    @SneakyThrows
    public FileInfo generatePdf(String username, String messageId, boolean isDraft) {
        log.info("generatePdf({},{}) (ENTER)", messageId, isDraft);
        CorrespondencePdfData pdfData;
        DomainAccount account;
        boolean statusSent = false;
        if (!isDraft) {
            MessageEntity messageEntity = this.messageRepository.getOne(Long.parseLong(messageId));
            if(!validateAccountAccess(username, messageEntity.getRecipientId(), accountService)){
                throw new InvalidDownloadAttachmentUserException("Not allowed to download other user's draft attachment");
            }
            pdfData = CorrespondencePdfDataMapper.MAPPER.map(messageEntity);
            account = this.accountService.getMainAccount(messageEntity.getRecipientId());
        } else {
            DraftEntity draftEntity = this.draftRepository.getOne(Long.parseLong(messageId));
            if(!validateAccountAccess(username, draftEntity.getUser(), accountService)) {
                throw new InvalidDownloadAttachmentUserException("Not allowed to download other user's draft attachment");
            }
            pdfData = CorrespondencePdfDataMapper.MAPPER.map(draftEntity);
            account = this.accountService.getMainAccount(draftEntity.getUser());
            statusSent = draftEntity.getDraftStatus().equalsIgnoreCase(MessageStatus.SENT.getValue());
        }
        PdfGenerator pdfGenerator = PdfGeneratorFactory.getPdfDataGenerator(ipoConfiguration, account, pdfData, isDraft);
        FileInfo pdfInfo = toPdf(pdfGenerator);
        if (isDraft && !statusSent) {
            pdfInfo.setFileName(pdfGenerator.getFileName());
            return pdfInfo;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream);
        ZipEntry entry = new ZipEntry(pdfInfo.getFileName());
        entry.setSize(pdfInfo.getFileContent().length);
        zipOut.putNextEntry(entry);
        zipOut.write(pdfInfo.getFileContent());
        zipOut.closeEntry();
        if (!isDraft) {
            addZipForAttachments(zipOut, messageId);
        } else {
            addZipForDraftAttachments(zipOut, messageId);
        }
        zipOut.close();
        return new FileInfo(byteArrayOutputStream.toByteArray(), "correspondence.zip");
    }

    @Override
    public FileInfo generatePdf(String username, String transactionId) {
        log.info("Shopping Cart payment invoice: generatePdf({}) (ENTER)", transactionId);
        InvoiceDetails invoiceDetails = paymentService.getInvoice(transactionId);
        InvoicePdfData invoicePdfData = InvoicePdfDataMapper.MAPPER.map(invoiceDetails);
        PdfGenerator pdfGenerator = PdfGeneratorFactory.getPdfDataGenerator(invoicePdfData);
        FileInfo pdfInfo = toPdf(pdfGenerator);
        pdfInfo.setFileName(pdfGenerator.getFileName());
        return pdfInfo;
    }

    /**
     * adds the message  attachments
     * @param zipOut the zipoutputstream
     * @param messageId the messageId
     */
    @SneakyThrows
    private void addZipForAttachments(ZipOutputStream zipOut, String messageId) {
        Set<String> names = new HashSet<>();
        //find the attachments
        List<Attachment> attachments = messageAttachmentService.getMessageAttachments(Long.parseLong(messageId));
        for (Attachment attachment : attachments) {
            String fileName = attachment.getName();
            if (!names.add(attachment.getName())) {
                String name = attachment.getName().substring(0, attachment.getName().lastIndexOf(".")) + "_" + LocalDateTime.now();
                String fileType = getFileType(attachment.getMimeType());
                fileName = name + "." + fileType;
            }
            FileResponse fileResponse = documentClient.getDocument(attachment.getUri(), attachment.getName());
            ZipEntry entry = new ZipEntry(fileName);
            entry.setSize(fileResponse.getBytes().length);
            zipOut.putNextEntry(entry);
            zipOut.write(fileResponse.getBytes());
            zipOut.closeEntry();
        }
    }

    /**
     * adds the draft attachments
     * @param zipOut the zipoutputstream
     * @param messageId the messageId
     */
    @SneakyThrows
    private void addZipForDraftAttachments(ZipOutputStream zipOut, String messageId) {
        Set<String> names = new HashSet<>();
        //find the attachments
        List<DraftAttachment> attachments = messageAttachmentService.getDraftAttachments(Long.parseLong(messageId));
        for (DraftAttachment attachment : attachments) {
            String fileName = attachment.getName();
            if (!names.add(attachment.getName())) {
                String name = attachment.getName().substring(0, attachment.getName().lastIndexOf(".")) + "_" + LocalDateTime.now();
                String fileType = getFileType(attachment.getMimeType());
                fileName = name + "." + fileType;
            }
            FileResponse fileResponse = documentClient.getDocument(attachment.getUri(), attachment.getName());
            ZipEntry entry = new ZipEntry(fileName);
            entry.setSize(fileResponse.getBytes().length);
            zipOut.putNextEntry(entry);
            zipOut.write(fileResponse.getBytes());
            zipOut.closeEntry();
        }
    }

    /**
     * Gets the type of specified file.
     *
     * @param fileMimeType the file MimeType
     * @return the file type
     */
    private String getFileType(String fileMimeType) {
        String fileMimeTypetype = null;
        for (MessageMediaTypes type : MessageMediaTypes.values()) {
            if (fileMimeType.equals(type.getValue())) {
                fileMimeTypetype = type.toString().toLowerCase();
            }
        }
        return fileMimeTypetype;
    }

    /**
     * creates a new pdf for an application
     *
     * @param pdfGenerator pdf implementation class
     * @return a fileInfo object
     */
    private FileInfo toPdf(PdfGenerator pdfGenerator) {
        PdfTemplate pdfTemplate = this.pdfTemplateConfiguration.getPdfTemplate(pdfGenerator.getPdfTemplateType());
        Map<String, Object> data = pdfGenerator.getData();
        String content = this.pdfTemplateHelper.processTemplate(pdfTemplate, data, Locale.getDefault());
        byte[] pdfBytes = Optional.of(writePdf(content, new PdfRendererBuilder())).map(ByteArrayOutputStream::toByteArray).orElse(new byte[]{});
        return new FileInfo(pdfBytes, pdfGenerator.getFileName());
    }

    private ByteArrayOutputStream writePdf(final String content, final PdfRendererBuilder pdfRendererBuilder) {
        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            pdfRendererBuilder.withHtmlContent(content, resourcesBaseUri);
            pdfRendererBuilder.toStream(os);
            pdfRendererBuilder.run();
            return os;
        } catch (final IOException ex) {
            throw new UserAreaException("Error generating pdf report", ex);
        }
    }

}