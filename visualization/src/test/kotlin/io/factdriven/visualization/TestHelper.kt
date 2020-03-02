package io.factdriven.visualization

import io.factdriven.definition.Definition
import io.factdriven.visualization.bpmn.transform
import io.factdriven.visualization.bpmn.translate
import org.camunda.bpm.model.bpmn.Bpmn
import java.io.File

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
fun render(definition: Definition) {
    val container = translate(definition)
    val bpmnModelInstance = transform(container)
    Bpmn.validateModel(bpmnModelInstance);
    val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
    Bpmn.writeModelToFile(file, bpmnModelInstance)
    if("Mac OS X" == System.getProperty("os.name"))
        Runtime.getRuntime().exec("open " + file.absoluteFile);
}