package ru.doccloud.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import ru.doccloud.service.DocumentExportService;
import ru.doccloud.service.document.dto.DocumentDTO;
import ru.doccloud.service.document.dto.ExportResultDTO;
import ru.doccloud.service.document.dto.SystemDTO;
import ru.doccloud.service.util.CmdHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Qualifier("server_shell_export_config")
public class ServerShellCmdExportServiceImpl implements DocumentExportService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerShellCmdExportServiceImpl.class);

    @Override
    public ExportResultDTO exportSelectedDocuments(SystemDTO exportConfig, String[] fields, UUID[] items) {
        LOGGER.debug("Export of selected documents: config={}, fields={}, items={}", exportConfig, fields, items);
        return exportDocuments(exportConfig, Lists.newArrayList(fields), Lists.newArrayList(items));
    }

    @Override
    public ExportResultDTO exportExplicitDocumentObjects(SystemDTO exportConfig, String[] fields, Iterable<DocumentDTO> docIterable) {
        LOGGER.debug("Export of explicit documents: config={}, fields={}, iterable={}", exportConfig, fields, docIterable);
        ArrayList<UUID> uuidList = new ArrayList<>();
        docIterable.forEach(doc -> uuidList.add(doc.getId()));
        return exportDocuments(exportConfig, Lists.newArrayList(fields), uuidList);
    }

    private ExportResultDTO exportDocuments(SystemDTO exportConfig, List<String> fields, List<UUID> items) {
        String cmdTemplate = exportConfig.getData().get("cmd_template").asText();
        if (!StringUtils.isEmpty(cmdTemplate)) {
            boolean iterative = exportConfig.getData().get("iterative_launch").asBoolean(true);
            if (iterative) {
                int successes = 0, failures = 0, index = 0;
                for (UUID item : items) {
                    try {
                        launchCommand(cmdTemplate, new ArgumentHolder(fields, Lists.newArrayList(item), index));
                        successes++;
                    } catch (Exception e) {
                        LOGGER.error("Error while iterative launching server shell command for the item {}", item, e);
                        failures++;
                    } finally {
                        index++;
                    }
                }
                return new ExportResultDTO(exportConfig.getSymbolicName(), failures > 0, String.format("%d/%d", successes, failures));
            } else {
                try {
                    launchCommand(cmdTemplate, new ArgumentHolder(fields, items, 0));
                    return new ExportResultDTO(exportConfig.getSymbolicName(), false, null);
                } catch (Exception e) {
                    LOGGER.error("Error while launching shell command for all items", e);
                    return new ExportResultDTO(exportConfig.getSymbolicName(), true, null);
                }
            }
        }
        return null;
    }

    private void launchCommand(String cmdTemplate, ArgumentHolder argument) {
        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression(cmdTemplate);
        EvaluationContext context = new StandardEvaluationContext(argument);
        String preparedCommand = (String) expression.getValue(context);

        LOGGER.debug("Prepared command: {}", preparedCommand);
        CmdHelper.callShell(preparedCommand, false);
    }

    private static class ArgumentHolder {
        public List<String> fields;
        public List<UUID> items;
        public int index;

        public ArgumentHolder(List<String> fields, List<UUID> items, int index) {
            this.fields = fields;
            this.items = items;
            this.index = index;
        }
    }

}
