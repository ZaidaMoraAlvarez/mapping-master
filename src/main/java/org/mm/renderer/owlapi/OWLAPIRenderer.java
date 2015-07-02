// TODO Needs to be seriously refactored. Way too long.

package org.mm.renderer.owlapi;

import org.mm.core.OWLEntityType;
import org.mm.parser.MappingMasterParserConstants;
import org.mm.parser.node.LiteralNode;
import org.mm.parser.node.MMDefaultPropertyTypeNode;
import org.mm.parser.node.OWLAllValuesFromNode;
import org.mm.parser.node.OWLClassOrRestrictionNode;
import org.mm.parser.node.OWLIndividualNode;
import org.mm.parser.node.TypeNode;
import org.mm.parser.node.ValueEncodingNode;
import org.mm.renderer.RendererException;
import org.mm.parser.node.AnnotationFactNode;
import org.mm.parser.node.FactNode;
import org.mm.parser.node.MMDefaultEntityTypeNode;
import org.mm.parser.node.MMDefaultPropertyValueTypeNode;
import org.mm.parser.node.MMDefaultValueEncodingNode;
import org.mm.parser.node.OWLClassDeclarationNode;
import org.mm.parser.node.OWLClassExpressionNode;
import org.mm.parser.node.OWLEnumeratedClassNode;
import org.mm.parser.node.OWLEquivalentToNode;
import org.mm.parser.node.OWLIndividualDeclarationNode;
import org.mm.parser.node.OWLIntersectionClassNode;
import org.mm.parser.node.OWLPropertyValueNode;
import org.mm.parser.node.OWLRestrictionNode;
import org.mm.parser.node.OWLSomeValuesFromNode;
import org.mm.parser.node.OWLSubclassOfNode;
import org.mm.parser.node.OWLUnionClassNode;
import org.mm.parser.node.ReferenceNode;
import org.mm.parser.node.SourceSpecificationNode;
import org.mm.parser.node.StringOrReferenceNode;
import org.mm.parser.node.ValueExtractionFunctionNode;
import org.mm.parser.node.ValueSpecificationItemNode;
import org.mm.parser.node.ValueSpecificationNode;
import org.mm.renderer.DefaultRenderer;
import org.mm.renderer.Rendering;
import org.mm.ss.SpreadSheetDataSource;
import org.mm.ss.SpreadsheetLocation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class OWLAPIRenderer extends DefaultRenderer implements MappingMasterParserConstants
{
  public static int NameEncodings[] = { MM_LOCATION, MM_DATA_VALUE, RDF_ID, RDFS_LABEL };
  public static int ReferenceValueTypes[] = { OWL_CLASS, OWL_THING, OWL_OBJECT_PROPERTY, OWL_DATA_PROPERTY, XSD_INT,
    XSD_STRING, XSD_FLOAT, XSD_DOUBLE, XSD_SHORT, XSD_BOOLEAN, XSD_TIME, XSD_DATETIME, XSD_DURATION };
  public static int PropertyTypes[] = { OWL_OBJECT_PROPERTY, OWL_DATA_PROPERTY };
  public static int PropertyValueTypes[] = ReferenceValueTypes;
  public static int DataPropertyValueTypes[] = { XSD_INT, XSD_STRING, XSD_FLOAT, XSD_DOUBLE, XSD_SHORT, XSD_BOOLEAN,
    XSD_TIME, XSD_DATETIME, XSD_DURATION };

  // Configuration options
  public int defaultValueEncoding = RDFS_LABEL;
  public int defaultOWLEntityType = OWL_CLASS;
  public int defaultOWLPropertyType = OWL_OBJECT_PROPERTY;
  public int defaultOWLPropertyValueType = XSD_STRING;
  public int defaultOWLDataPropertyValueType = XSD_STRING;

  public int defaultEmptyLocationDirective = MM_PROCESS_IF_EMPTY_LOCATION;
  public int defaultEmptyRDFIDDirective = MM_PROCESS_IF_EMPTY_ID;
  public int defaultEmptyRDFSLabelDirective = MM_PROCESS_IF_EMPTY_LABEL;
  public int defaultIfExistsDirective = MM_RESOLVE_IF_EXISTS;
  public int defaultIfNotExistsDirective = MM_CREATE_IF_NOT_EXISTS;

  private boolean createEntities = false;

  private final OWLOntology ontology;
  private final OWLDataFactory owlDataFactory;
  private final OWLAPIObjectHandler owlObjectHandler;
  private SpreadSheetDataSource dataSource;

  public OWLAPIRenderer(OWLOntology ontology, SpreadSheetDataSource dataSource)
  {
    this.ontology = ontology;
    this.owlDataFactory = ontology.getOWLOntologyManager().getOWLDataFactory();
    this.owlObjectHandler = new OWLAPIObjectHandler(ontology);
    this.dataSource = dataSource;
  }

  public void setDataSource(SpreadSheetDataSource dataSource)
  {
    this.dataSource = dataSource;
  }

  public void reset()
  {
    owlObjectHandler.reset();
  }

  public SpreadSheetDataSource getDataSource()
  {
    return dataSource;
  }

  public void setCreateEntities(boolean createEntities)
  {
    this.createEntities = createEntities;
  }

  @Override public Rendering renderMMDefaultEntityType(MMDefaultEntityTypeNode defaultEntityTypeNode)
    throws RendererException
  {
    defaultOWLEntityType = defaultEntityTypeNode.getEntityType().getType();
    // System.err.println("setting default entity type to " + mmDefaultEntityType.getEntityType().getTypeName());
    return new Rendering(defaultEntityTypeNode.getEntityType().getTypeName());
  }

  @Override public Rendering renderMMDefaultValueEncoding(MMDefaultValueEncodingNode defaultValueEncodingNode)
    throws RendererException
  {
    this.defaultValueEncoding = defaultValueEncodingNode.getEncodingType();
    // System.err.println("setting default value encoding to " + mmDefaultValueEncoding.getEncodingTypeName());
    return new Rendering(defaultValueEncodingNode.getEncodingTypeName());
  }

  @Override public Rendering renderMMDefaultPropertyValueType(
    MMDefaultPropertyValueTypeNode defaultPropertyValueTypeNode) throws RendererException
  {
    defaultOWLPropertyValueType = defaultPropertyValueTypeNode.getType();
    // System.err.println("setting default property value type to " + mmDefaultPropertyValueType.getTypeName());
    return new Rendering(defaultPropertyValueTypeNode.getTypeName());
  }

  @Override public Rendering renderMMDefaultPropertyType(MMDefaultPropertyTypeNode defaultPropertyTypeNode)
    throws RendererException
  {
    defaultOWLPropertyType = defaultPropertyTypeNode.getType();
    // System.err.println("setting default property value type to " + mmDefaultPropertyType.getTypeName());
    return new Rendering(defaultPropertyTypeNode.getTypeName());
  }

  // TODO Too long. Clean up.
  @Override public Rendering renderReference(ReferenceNode referenceNode) throws RendererException
  {
    SpreadsheetLocation location = getLocation(referenceNode.getSourceSpecificationNode());
    String defaultNamespace = getReferenceNamespace(referenceNode);
    String language = getReferenceLanguage(referenceNode);
    Rendering rendering = new Rendering();

    rendering.logLine("<<<<<<<<<<<<<<<<<<<< Rendering reference [" + referenceNode + "] <<<<<<<<<<<<<<<<<<<<");

    String locationValue = processLocationValue(location, referenceNode, rendering);

    if (locationValue.equals("") && referenceNode.getActualEmptyLocationDirective() == MM_SKIP_IF_EMPTY_LOCATION)
      return rendering;

    if (createEntities) {
      OWLEntityType entityType = referenceNode.getEntityTypeNode().getEntityType();

      if (entityType.isUntyped())
        throw new RendererException("untyped entity for reference " + referenceNode);

      if (entityType.isOWLDataValue()) { // OWL data value
        String processedOWLDataValue = processOWLDataValue(location, locationValue, entityType, referenceNode,
          rendering);

        if (processedOWLDataValue.equals("")
          && referenceNode.getActualEmptyDataValueDirective() == MM_SKIP_IF_EMPTY_DATA_VALUE)
          return rendering;

        rendering.addText(processedOWLDataValue);
      } else { // OWL Class, Individual, ObjectProperty, or DataProperty
        String rdfID = processRDFIDValue(locationValue, referenceNode, rendering);
        String rdfsLabelText = processRDFSLabelText(locationValue, referenceNode, rendering);
        OWLEntity entity = this.owlObjectHandler
          .createOrResolveOWLEntity(location, locationValue, entityType, rdfID, rdfsLabelText, defaultNamespace,
            language, referenceNode.getReferenceDirectives());
        addDefiningTypes(entityType, entity, referenceNode);
        rendering.addText(rdfID);
      }
    }

    if (!rendering.nothingRendered())
      rendering.logLine(">>>>>>>>>>>>>>>>>>>> Reference [" + referenceNode.toString() + "] rendered as " + referenceNode
        .getEntityTypeNode() + " " + rendering.getRendering() + " >>>>>>>>>>>>>>>>>>>>");
    else
      rendering
        .logLine(">>>>>>>>>>>>>>>>>>>> Reference [" + referenceNode + "] - nothing rendered >>>>>>>>>>>>>>>>>>>>");

    return rendering;
  }

  @Override public Rendering renderOWLIndividualDeclaration(OWLIndividualDeclarationNode individualDeclarationNode)
    throws RendererException
  {
    Rendering individualDeclarationRendering = new Rendering();

    individualDeclarationRendering
      .logLine("=====================OWLIndividualDeclaration================================");
    individualDeclarationRendering.logLine("MappingMaster DSL expression: " + individualDeclarationNode);
    if (dataSource.hasCurrentLocation())
      individualDeclarationRendering
        .logLine("********************Current location: " + dataSource.getCurrentLocation() + "*************");

    if (createEntities) {
      Rendering declaredIndividualRendering = renderOWLIndividual(individualDeclarationNode.getOWLIndividualNode());
      if (declaredIndividualRendering.nothingRendered()) {
        individualDeclarationRendering
          .logLine("Skipping OWL individual declaration because of missing individual name");
      } else {
        if (individualDeclarationNode.hasFacts()) { // We have a Facts: clause
          List<FactNode> factNodes = individualDeclarationNode.getFactNodes();
          processFactsClause(individualDeclarationRendering, declaredIndividualRendering, factNodes);
        }

        if (individualDeclarationNode.hasAnnotations()) { // We have an Annotations: clause
          List<AnnotationFactNode> annotationFactNodes = individualDeclarationNode.getAnnotationNodes();
          processAnnotationClause(individualDeclarationRendering, declaredIndividualRendering, annotationFactNodes);
        }

        if (individualDeclarationNode.hasSameAs()) // We have a SameAs: clause
          processSameAsClause(individualDeclarationNode, declaredIndividualRendering);

        if (individualDeclarationNode.hasDifferentFrom()) // We have a DifferentFrom: clause
          processDifferentFromClause(individualDeclarationNode, declaredIndividualRendering);

        if (individualDeclarationNode.hasTypes()) { // We have a Types: clause
          processTypesClause(individualDeclarationNode, individualDeclarationRendering, declaredIndividualRendering);
        }
        individualDeclarationRendering = declaredIndividualRendering; // Final rendering is declared individual
      }
    } else
      individualDeclarationRendering = super.renderOWLIndividualDeclaration(individualDeclarationNode);

    return individualDeclarationRendering;
  }

  @Override public Rendering renderOWLClassDeclaration(OWLClassDeclarationNode classDeclarationNode)
    throws RendererException
  {
    Rendering rendering = new Rendering();

    rendering.logLine("=====================OWLClassDeclaration================================");
    rendering.logLine("MappingMaster DSL expression: " + classDeclarationNode);
    if (dataSource.hasCurrentLocation())
      rendering.logLine("********************Current location: " + dataSource.getCurrentLocation() + "*************");

    if (createEntities) {
      Rendering declaredClassRendering = renderOWLNamedClass(classDeclarationNode.getOWLNamedClassNode());

      if (declaredClassRendering.nothingRendered()) {
        rendering.logLine("processReference: skipping OWL class declaration because of missing class");
      } else {
        if (classDeclarationNode.hasSubclassOf()) {
          for (OWLSubclassOfNode subclassOfNode : classDeclarationNode.getSubclassOfNodes()) {
            for (OWLClassExpressionNode classExpressionNode : subclassOfNode.getClassExpressionNodes()) {
              Rendering classExpressionRendering = renderOWLClassExpression(classExpressionNode);
              if (classExpressionRendering.nothingRendered())
                rendering.logLine(
                  "processReference: skipping subclass declaration [" + subclassOfNode + "] because of missing class");
              else {
                String classExpressionID = classExpressionRendering.getRendering();
                String declaredClassID = declaredClassRendering.getRendering();
                OWLClassExpression declaredClass = this.owlObjectHandler.getOWLClass(declaredClassID);
                OWLClassExpression classExpression = this.owlObjectHandler.getOWLClassExpression(classExpressionID);
                OWLSubClassOfAxiom axiom = this.owlDataFactory.getOWLSubClassOfAxiom(classExpression, declaredClass);
                addOWLAxiom(axiom);
              }
            }
          }
        }

        if (classDeclarationNode.hasEquivalentTo()) {
          for (OWLEquivalentToNode equivalentToNode : classDeclarationNode.getEquivalentToNodes()) {
            for (OWLClassExpressionNode classExpressionNode : equivalentToNode.getClassExpressionNodes()) {
              Rendering classExpressionRendering = renderOWLClassExpression(classExpressionNode);
              if (classExpressionRendering.nothingRendered())
                rendering.logLine("processReference: skipping eqivalent declaration [" + equivalentToNode
                  + "] because of missing class");
              else {
                String declaredClassID = declaredClassRendering.getRendering();
                String classExpressionID = classExpressionRendering.getRendering();
                OWLClassExpression declaredClass = this.owlObjectHandler.getOWLClass(declaredClassID);
                OWLClassExpression classExpression = this.owlObjectHandler.getOWLClassExpression(classExpressionID);
                OWLEquivalentClassesAxiom axiom = owlDataFactory
                  .getOWLEquivalentClassesAxiom(classExpression, declaredClass);
                // TODO Add axiom to ontology
              }
            }
          }
        }

        if (classDeclarationNode.hasAnnotations()) {
          for (AnnotationFactNode annotationFactNode : classDeclarationNode.getAnnotationFactNodes()) {
            Rendering propertyRendering = renderOWLProperty(annotationFactNode.getOWLPropertyNode());
            OWLPropertyValueNode propertyValueNode = annotationFactNode.getOWLPropertyValueNode();
            Rendering propertyValueRendering = renderOWLPropertyValue(propertyValueNode);

            if (propertyRendering.nothingRendered()) {
              rendering.logLine("processReference: skipping OWL annotation clause [" + annotationFactNode
                + "] because of missing property name");
              continue;
            }

            if (propertyValueNode.isReference()) {
              ReferenceNode referenceNode = propertyValueNode.getReferenceNode();
              String propertyShortName = propertyRendering.getRendering();
              if (!referenceNode.hasExplicitlySpecifiedEntityType() && this.owlObjectHandler
                .isOWLObjectProperty(propertyShortName))
                referenceNode.updateEntityType(OWL_THING);
            }

            if (propertyValueRendering.nothingRendered()) {
              rendering.logLine("processReference: skipping OWL annotation clause [" + annotationFactNode
                + "] because of missing property value");
              continue;
            }
            String classShortName = declaredClassRendering.getRendering();
            String propertyShortName = propertyRendering.getRendering();
            OWLClass cls = this.owlObjectHandler.getOWLClass(classShortName);
            OWLAnnotationProperty property = this.owlObjectHandler.getOWLAnnotationProperty(propertyShortName);
            String propertyValue = propertyValueRendering.getRendering();
            createOWLAnnotationAssertionAxiom(cls, property, propertyValue, propertyValueNode);
          }
        }
        rendering = declaredClassRendering;
      }
    } else
      rendering = super.renderOWLClassDeclaration(classDeclarationNode);

    return rendering;
  }

  @Override public Rendering renderOWLClassExpression(OWLClassExpressionNode owlClassExpressionNode)
    throws RendererException
  {
    if (createEntities)
      return renderOWLUnionClass(owlClassExpressionNode.getOWLUnionClassNode());
    else
      return super.renderOWLClassExpression(owlClassExpressionNode);
  }

  @Override public Rendering renderOWLUnionClass(OWLUnionClassNode unionClassNode) throws RendererException
  {
    Rendering rendering = new Rendering();

    if (createEntities) {
      Set<OWLClassExpression> classes = new HashSet<>();

      for (OWLIntersectionClassNode intersectionClassNode : unionClassNode.getOWLIntersectionClasseNodes()) {
        Rendering classRendering = renderOWLIntersectionClass(intersectionClassNode);
        String classExpressionID = classRendering.getRendering();
        if (!classRendering.nothingRendered())
          classes.add(this.owlObjectHandler.getOWLClassExpression(classExpressionID));
      }

      if (!classes.isEmpty()) {
        OWLObjectUnionOf restriction = this.owlDataFactory.getOWLObjectUnionOf(classes);
        String restrictionID = this.owlObjectHandler.registerOWLClassExpression(restriction);
        rendering = new Rendering(restrictionID);
      }
    } else
      return super.renderOWLUnionClass(unionClassNode);

    return rendering;
  }

  @Override public Rendering renderOWLIntersectionClass(OWLIntersectionClassNode intersectionClassNode)
    throws RendererException
  {
    Rendering rendering = new Rendering();

    if (createEntities) {
      Set<OWLClassExpression> classes = new HashSet<>();

      for (OWLClassOrRestrictionNode classOrRestrictionNode : intersectionClassNode.getOWLClassesOrRestrictionNodes()) {
        Rendering classRendering = renderOWLClassOrRestriction(classOrRestrictionNode);
        String classExpressionID = classRendering.getRendering();
        if (!classRendering.nothingRendered())
          classes.add(this.owlObjectHandler.getOWLClassExpression(classExpressionID));
      }

      if (!classes.isEmpty()) {
        OWLObjectIntersectionOf restriction = this.owlDataFactory.getOWLObjectIntersectionOf(classes);
        String restrictionID = this.owlObjectHandler.registerOWLClassExpression(restriction);
        rendering = new Rendering(restrictionID);
      }
    } else
      return super.renderOWLIntersectionClass(intersectionClassNode);

    return rendering;
  }

  @Override public Rendering renderOWLEnumeratedClass(OWLEnumeratedClassNode enumeratedClassNode)
    throws RendererException
  {
    Rendering rendering = new Rendering();

    if (createEntities) {
      Set<OWLNamedIndividual> individuals = new HashSet<>();

      for (OWLIndividualNode owlIndividualNode : enumeratedClassNode.getOWLIndividualNodes()) {
        Rendering individualRendering = renderOWLIndividual(owlIndividualNode);
        String individualShortName = individualRendering.getRendering();
        if (!individualRendering.nothingRendered()) {
          OWLNamedIndividual individual = this.owlObjectHandler.getOWLNamedIndividual(individualShortName);
          individuals.add(individual);
        }
      }

      if (!individuals.isEmpty()) {
        OWLObjectOneOf restriction = this.owlDataFactory.getOWLObjectOneOf(individuals);
        String restrictionID = this.owlObjectHandler.registerOWLClassExpression(restriction);
        rendering = new Rendering(restrictionID);
      }
    } else
      return super.renderOWLEnumeratedClass(enumeratedClassNode);

    return rendering;
  }

  @Override public Rendering renderOWLClassOrRestriction(OWLClassOrRestrictionNode classOrRestrictionNode)
    throws RendererException
  {
    Rendering rendering;

    if (createEntities) {
      Rendering classRendering;

      if (classOrRestrictionNode.hasOWLEnumeratedClass())
        classRendering = renderOWLEnumeratedClass(classOrRestrictionNode.getOWLEnumeratedClassNode());
      else if (classOrRestrictionNode.hasOWLUnionClass())
        classRendering = renderOWLUnionClass(classOrRestrictionNode.getOWLUnionClassNode());
      else if (classOrRestrictionNode.hasOWLRestriction())
        classRendering = renderOWLRestriction(classOrRestrictionNode.getOWLRestrictionNode());
      else if (classOrRestrictionNode.hasOWLNamedClass())
        classRendering = renderOWLNamedClass(classOrRestrictionNode.getOWLNamedClassNode());
      else
        throw new RendererException("unknown OWLClassOrRestriction node " + classOrRestrictionNode);

      if (classOrRestrictionNode.getIsNegated()) {
        String classExpressionID = classRendering.getRendering();
        OWLClassExpression classExpression = this.owlObjectHandler.getOWLClassExpression(classExpressionID);
        OWLObjectComplementOf restriction = this.owlDataFactory.getOWLObjectComplementOf(classExpression);
        String restrictionID = this.owlObjectHandler.registerOWLClassExpression(restriction);
        rendering = new Rendering(restrictionID);
      } else
        rendering = classRendering;
    } else
      rendering = super.renderOWLClassOrRestriction(classOrRestrictionNode);

    return rendering;
  }

  // TODO: separate into data and object restrictions
  @Override public Rendering renderOWLRestriction(OWLRestrictionNode restrictionNode) throws RendererException
  {
    Rendering propertyNodeRendering = renderOWLProperty(restrictionNode.getOWLPropertyNode());

    if (createEntities) {
      if (!propertyNodeRendering.nothingRendered()) {
        String propertyShortName = propertyNodeRendering.getRendering();
        if (this.owlObjectHandler.isOWLDataProperty(propertyShortName)) {
          OWLDataProperty property = this.owlObjectHandler.getOWLDataProperty(propertyShortName);
          if (restrictionNode.hasOWLMinCardinality()) {
            int cardinality = restrictionNode.getOWLMinCardinalityNode().getCardinality();
            OWLDataMinCardinality restriction = this.owlDataFactory.getOWLDataMinCardinality(cardinality, property);
          } else if (restrictionNode.hasOWLMaxCardinality()) {
            int cardinality = restrictionNode.getOWLMaxCardinalityNode().getCardinality();
            OWLDataMaxCardinality restriction = this.owlDataFactory.getOWLDataMaxCardinality(cardinality, property);
          } else if (restrictionNode.hasOWLCardinality()) {
            int cardinality = restrictionNode.getOWLCardinalityNode().getCardinality();
            OWLDataExactCardinality restriction = this.owlDataFactory.getOWLDataExactCardinality(cardinality, property);
          } else if (restrictionNode.hasOWLHasValue()) {
            OWLPropertyValueNode propertyValueNode = restrictionNode.getOWLHasValueNode().getOWLPropertyValueNode();
            if (!propertyValueNode.isLiteral())
              throw new RendererException("expecting data value for data has value restriction " + restrictionNode);
            OWLLiteral literal = getOWLLiteral(propertyValueNode);
            OWLDataHasValue restriction = this.owlDataFactory.getOWLDataHasValue(property, literal);
          } else if (restrictionNode.hasOWLAllValuesFrom()) {
            OWLAllValuesFromNode allValuesFromNode = restrictionNode.getOWLAllValuesFromNode();
            if (!allValuesFromNode.hasOWLAllValuesFromDataType())
              throw new RendererException("expecting data value for all values data restriction " + restrictionNode);
            OWLDatatype datatype = this.owlObjectHandler
              .getOWLDatatype(allValuesFromNode.getOWLAllValuesFromDataTypeNode().getDataTypeName());
            OWLDataAllValuesFrom restriction = this.owlDataFactory.getOWLDataAllValuesFrom(property, datatype);
          } else if (restrictionNode.hasOWLSomeValuesFrom()) {
            OWLSomeValuesFromNode someValuesFromNode = restrictionNode.getOWLSomeValuesFromNode();
            if (!someValuesFromNode.hasOWLSomeValuesFromDataType())
              throw new RendererException("expecting data value for some values data restriction " + restrictionNode);
            OWLDatatype datatype = this.owlObjectHandler
              .getOWLDatatype(someValuesFromNode.getOWLSomeValuesFromDataTypeNode().getDataTypeName());
            OWLDataSomeValuesFrom restriction = this.owlDataFactory.getOWLDataSomeValuesFrom(property, datatype);
          }
        } else { // Object property
          OWLObjectProperty property = this.owlObjectHandler.getOWLObjectProperty(propertyShortName);
          if (restrictionNode.hasOWLMinCardinality()) {
            int cardinality = restrictionNode.getOWLMinCardinalityNode().getCardinality();
            OWLObjectMinCardinality restriction = this.owlDataFactory.getOWLObjectMinCardinality(cardinality, property);
          } else if (restrictionNode.hasOWLMaxCardinality()) {
            int cardinality = restrictionNode.getOWLMaxCardinalityNode().getCardinality();
            OWLObjectMaxCardinality restriction = this.owlDataFactory.getOWLObjectMaxCardinality(cardinality, property);
          } else if (restrictionNode.hasOWLCardinality()) {
            int cardinality = restrictionNode.getOWLCardinalityNode().getCardinality();
            OWLObjectExactCardinality restriction = this.owlDataFactory
              .getOWLObjectExactCardinality(cardinality, property);
          } else if (restrictionNode.hasOWLHasValue()) {
            OWLPropertyValueNode propertyValueNode = restrictionNode.getOWLHasValueNode().getOWLPropertyValueNode();
            if (propertyValueNode.isLiteral())
              throw new RendererException("expecting class for object has value restriction " + restrictionNode);
            Rendering individualRendering = renderOWLPropertyValue(propertyValueNode);
            String individualShortName = individualRendering.getRendering();
            OWLNamedIndividual individual = this.owlObjectHandler.getOWLNamedIndividual(individualShortName);
            OWLObjectHasValue restriction = this.owlDataFactory.getOWLObjectHasValue(property, individual);
          } else if (restrictionNode.hasOWLAllValuesFrom()) {
            OWLAllValuesFromNode allValuesFromNode = restrictionNode.getOWLAllValuesFromNode();
            if (allValuesFromNode.hasOWLAllValuesFromDataType())
              throw new RendererException("expecting class for all values object restriction " + restrictionNode);
            Rendering classRendering;
            if (allValuesFromNode.getOWLAllValuesFromClassNode().hasOWLClassExpression())
              classRendering = renderOWLClassExpression(
                allValuesFromNode.getOWLAllValuesFromClassNode().getOWLClassExpressionNode());
            else
              classRendering = renderOWLNamedClass(
                allValuesFromNode.getOWLAllValuesFromClassNode().getOWLNamedClassNode());
            if (!classRendering.nothingRendered()) {
              String classExpressionID = classRendering.getRendering();
              OWLClassExpression cls = this.owlObjectHandler.getOWLClassExpression(classExpressionID);
              OWLObjectAllValuesFrom restriction = this.owlDataFactory.getOWLObjectAllValuesFrom(property, cls);
            }
          } else if (restrictionNode.hasOWLSomeValuesFrom()) {
            OWLSomeValuesFromNode someValuesFromNode = restrictionNode.getOWLSomeValuesFromNode();
            if (someValuesFromNode.hasOWLSomeValuesFromDataType())
              throw new RendererException("expecting class for object some values  restriction " + restrictionNode);
            Rendering classRendering;
            if (someValuesFromNode.getOWLSomeValuesFromClassNode().hasOWLClassExpression())
              classRendering = renderOWLClassExpression(
                someValuesFromNode.getOWLSomeValuesFromClassNode().getOWLClassExpressionNode());
            else
              classRendering = renderOWLNamedClass(
                someValuesFromNode.getOWLSomeValuesFromClassNode().getOWLNamedClassNode());
            if (!classRendering.nothingRendered()) {
              String classExpressionID = classRendering.getRendering();
              OWLClassExpression cls = this.owlObjectHandler.getOWLClassExpression(classExpressionID);
              OWLObjectSomeValuesFrom restriction = this.owlDataFactory.getOWLObjectSomeValuesFrom(property, cls);
            }
          }
        }
      }
    }
    return null; // TODO
  }

  private void processTypesClause(OWLIndividualDeclarationNode individualDeclarationNode,
    Rendering individualDeclarationRendering, Rendering declaredIndividualRendering) throws RendererException
  {
    for (TypeNode typeNode : individualDeclarationNode.getTypeNodes().getTypeNodes()) {
      Rendering typeRendering = renderType(typeNode);
      if (typeRendering.nothingRendered()) {
        individualDeclarationRendering.logLine(
          "processReference: skipping OWL type declaration clause [" + typeNode + "] for individual "
            + declaredIndividualRendering + " because of missing type");
        continue;
      }
      String individualShortName = declaredIndividualRendering.getRendering();
      String classShortName = typeRendering.getRendering();
      OWLNamedIndividual individual = this.owlObjectHandler.getOWLNamedIndividual(individualShortName);
      OWLClass cls = this.owlObjectHandler.getOWLClass(classShortName);
      OWLClassAssertionAxiom axiom = this.owlDataFactory.getOWLClassAssertionAxiom(cls, individual);

      addOWLAxiom(axiom);
    }
  }

  private void processSameAsClause(OWLIndividualDeclarationNode individualDeclarationNode,
    Rendering declaredIndividualRendering) throws RendererException
  {
    for (OWLIndividualNode sameAsIndividualNode : individualDeclarationNode.getSameAsNode().getIndividualNodes()) {
      Rendering sameAsIndividualRendering = renderOWLIndividual(sameAsIndividualNode);
      if (!sameAsIndividualRendering.nothingRendered()) {
        String individual1ShortName = declaredIndividualRendering.getRendering();
        String individual2ShortName = sameAsIndividualRendering.getRendering();
        OWLNamedIndividual individual1 = this.owlObjectHandler.getOWLNamedIndividual(individual1ShortName);
        OWLNamedIndividual individual2 = this.owlObjectHandler.getOWLNamedIndividual(individual2ShortName);
        OWLSameIndividualAxiom axiom = owlDataFactory.getOWLSameIndividualAxiom(individual1, individual2);

        addOWLAxiom(axiom);
      }
    }
  }

  private void processDifferentFromClause(OWLIndividualDeclarationNode individualDeclarationNode,
    Rendering declaredIndividualRendering) throws RendererException
  {
    for (OWLIndividualNode differentFromIndividualNode : individualDeclarationNode.getDifferentFromNode()
      .getIndividualNodes()) {
      Rendering differentFromIndividualRendering = renderOWLIndividual(differentFromIndividualNode);
      if (!differentFromIndividualRendering.nothingRendered()) {
        String individual1ShortName = declaredIndividualRendering.getRendering();
        String individual2ShortName = differentFromIndividualRendering.getRendering();
        OWLNamedIndividual individual1 = this.owlObjectHandler.getOWLNamedIndividual(individual1ShortName);
        OWLNamedIndividual individual2 = this.owlObjectHandler.getOWLNamedIndividual(individual2ShortName);
        OWLDifferentIndividualsAxiom axiom = owlDataFactory.getOWLDifferentIndividualsAxiom(individual1, individual2);

        addOWLAxiom(axiom);
      }
    }
  }

  private void processAnnotationClause(Rendering individualDeclarationRendering, Rendering declaredIndividualRendering,
    List<AnnotationFactNode> annotationFactNodes) throws RendererException
  {
    for (AnnotationFactNode annotationFact : annotationFactNodes) {
      Rendering propertyRendering = renderOWLProperty(annotationFact.getOWLPropertyNode());
      OWLPropertyValueNode propertyValueNode = annotationFact.getOWLPropertyValueNode();
      Rendering propertyValueRendering = renderOWLPropertyValue(propertyValueNode);

      if (propertyRendering.nothingRendered()) {
        individualDeclarationRendering
          .logLine("Skipping OWL annotation clause [" + annotationFact + "] because of missing property name");
        continue;
      }

      if (propertyValueNode.isReference()) { // We have an object property so tell the reference
        ReferenceNode referenceNode = propertyValueNode.getReferenceNode();
        String propertyShortName = propertyRendering.getRendering();
        if (!referenceNode.hasExplicitlySpecifiedEntityType() && this.owlObjectHandler
          .isOWLObjectProperty(propertyShortName))
          referenceNode.updateEntityType(OWL_THING);
      }

      if (propertyValueRendering.nothingRendered()) {
        individualDeclarationRendering
          .logLine("Skipping OWL annotation clause [" + annotationFact + "] because of missing property value");
        continue;
      }

      String individualShortName = declaredIndividualRendering.getRendering();
      String propertyShortName = propertyRendering.getRendering();
      String propertyValue = propertyValueRendering.getRendering();
      OWLNamedIndividual individual = this.owlObjectHandler.getOWLNamedIndividual(individualShortName);
      OWLAnnotationProperty property = this.owlObjectHandler.getOWLAnnotationProperty(propertyShortName);

      createOWLAnnotationAssertionAxiom(individual, property, propertyValue, propertyValueNode);
    }
  }

  private void processFactsClause(Rendering individualDeclarationRendering, Rendering declaredIndividualRendering,
    List<FactNode> factNodes) throws RendererException
  {
    for (FactNode factNode : factNodes) {
      Rendering propertyRendering = renderOWLProperty(factNode.getOWLPropertyNode());
      String propertyShortName = propertyRendering.getRendering();
      OWLPropertyValueNode propertyValueNode = factNode.getOWLPropertyValueNode();
      Rendering propertyValueRendering = renderOWLPropertyValue(propertyValueNode);

      if (propertyRendering.nothingRendered()) {
        individualDeclarationRendering
          .logLine("Skipping OWL fact declaration clause [" + factNode + "] because of missing property name");
        continue;
      }
      if (propertyValueRendering.nothingRendered()) {
        individualDeclarationRendering
          .logLine("Skipping OWL fact declaration clause [" + factNode + "] because of missing property value");
        continue;
      }
      if (propertyValueNode.isReference()) { // We have an object property so tell the reference the type
        ReferenceNode reference = propertyValueNode.getReferenceNode();

        if (!reference.hasExplicitlySpecifiedEntityType() && this.owlObjectHandler
          .isOWLObjectProperty(propertyShortName))
          reference.updateEntityType(OWL_THING);
      }

      if (this.owlObjectHandler.isOWLObjectProperty(propertyShortName)) {
        String individualShortName = declaredIndividualRendering.getRendering();
        String propertyValue = propertyValueRendering.getRendering();
        OWLNamedIndividual individual = this.owlObjectHandler.getOWLNamedIndividual(individualShortName);
        OWLObjectProperty property = this.owlObjectHandler.getOWLObjectProperty(propertyShortName);

        OWLObjectPropertyAssertionAxiom axiom = createOWLObjectPropertyAssertionAxiom(individual, property,
          propertyValue, propertyValueNode);
        addOWLAxiom(axiom);
      } else if (this.owlObjectHandler.isOWLDataProperty(propertyShortName)) {
        String individualShortName = declaredIndividualRendering.getRendering();
        String propertyValue = propertyValueRendering.getRendering();
        OWLNamedIndividual individual = this.owlObjectHandler.getOWLNamedIndividual(individualShortName);
        OWLDataProperty property = this.owlObjectHandler.getOWLDataProperty(propertyShortName);

        OWLDataPropertyAssertionAxiom axiom = createOWLDataPropertyAssertionAxiom(individual, property, propertyValue,
          propertyValueNode);
        addOWLAxiom(axiom);
      } else {
        individualDeclarationRendering.logLine(
          "Skipping OWL fact declaration clause [" + factNode + "] because property is an annotation property");
        continue;
      }
    }
  }

  // TODO: tentative. Need a more principled way of finding and invoking functions. What about calls to Excel?
  private String processValueExtractionFunction(ValueExtractionFunctionNode valueExtractionFunctionNode, String value)
    throws RendererException
  {
    List<String> arguments = new ArrayList<>();
    String functionName = valueExtractionFunctionNode.getFunctionName();
    boolean hasExplicitArguments = valueExtractionFunctionNode.hasArguments();
    String processedValue;

    if (valueExtractionFunctionNode.hasArguments()) {
      for (StringOrReferenceNode argumentNode : valueExtractionFunctionNode.getArgumentNodes()) {
        Rendering argumentRendering = renderStringOrReference(argumentNode);
        arguments.add(argumentRendering.getRendering());
      }
    }

    switch (valueExtractionFunctionNode.getFunctionID()) {
    case MM_TO_UPPER_CASE:
      if (hasExplicitArguments) {
        if (arguments.size() != 1)
          throw new RendererException("function " + functionName + " expecting one argument, got " + arguments.size());
        processedValue = arguments.get(0).toUpperCase();
      } else
        processedValue = value.toUpperCase();
      break;
    case MM_TO_LOWER_CASE:
      if (hasExplicitArguments) {
        if (arguments.size() != 1)
          throw new RendererException(
            "function " + functionName + " expecting only one argument, got " + arguments.size());
        processedValue = arguments.get(0).toLowerCase();
      } else
        processedValue = value.toLowerCase();
      break;
    case MM_TRIM:
      if (hasExplicitArguments) {
        if (arguments.size() != 1)
          throw new RendererException(
            "function " + functionName + " expecting only one argument, got " + arguments.size());
        processedValue = arguments.get(0).trim();
      } else
        processedValue = value.trim();
      break;
    case MM_REVERSE:
      if (hasExplicitArguments) {
        if (arguments.size() != 1)
          throw new RendererException(
            "function " + functionName + " expecting only one argument, got " + arguments.size());
        processedValue = reverse(arguments.get(0));
      } else
        processedValue = reverse(value);
      break;
    case MM_CAPTURING:
      if (arguments.size() == 1) {
        processedValue = processCapturingExpression(value, arguments.get(0));
      } else if (arguments.size() == 2) {
        processedValue = processCapturingExpression(arguments.get(0), arguments.get(1));
      } else
        throw new RendererException(
          "function " + functionName + " expecting two or three arguments, got " + arguments.size());
      break;
    case MM_PREPEND:
      if (arguments.size() == 1) {
        processedValue = arguments.get(0) + value;
      } else if (arguments.size() == 2) {
        processedValue = arguments.get(0) + arguments.get(1);
      } else
        throw new RendererException(
          "function " + functionName + " expecting two or three arguments, got " + arguments.size());
      break;
    case MM_APPEND:
      if (arguments.size() == 1) {
        processedValue = value + arguments.get(0);
      } else if (arguments.size() == 2) {
        processedValue = arguments.get(0) + arguments.get(1);
      } else
        throw new RendererException(
          "function " + functionName + " expecting two or three arguments, got " + arguments.size());
      break;
    case MM_REPLACE:
      if (arguments.size() == 2) {
        processedValue = value.replace(arguments.get(0), arguments.get(1));
      } else if (arguments.size() == 3) {
        processedValue = arguments.get(0).replace(arguments.get(1), arguments.get(2));
      } else
        throw new RendererException(
          "function " + functionName + " expecting two or three arguments, got " + arguments.size());
      break;
    case MM_REPLACE_ALL:
      if (arguments.size() == 2) {
        processedValue = value.replaceAll(arguments.get(0), arguments.get(1));
      } else if (arguments.size() == 3) {
        processedValue = arguments.get(0).replaceAll(arguments.get(1), arguments.get(2));
      } else
        throw new RendererException(
          "function " + functionName + " expecting two or three arguments, got " + arguments.size());
      break;
    case MM_REPLACE_FIRST:
      if (arguments.size() == 2) {
        processedValue = value.replaceFirst(arguments.get(0), arguments.get(1));
      } else if (arguments.size() == 3) {
        processedValue = arguments.get(0).replaceFirst(arguments.get(1), arguments.get(2));
      } else
        throw new RendererException(
          "function " + functionName + " expecting two or three arguments, got " + arguments.size());
      break;
    default:
      throw new RendererException("unknown mapping function " + valueExtractionFunctionNode.getFunctionName() + ")");
    }
    return processedValue;
  }

  private OWLLiteral getOWLLiteral(OWLPropertyValueNode propertyValueNode) throws RendererException
  {
    LiteralNode literalNode = propertyValueNode.getLiteralNode();

    if (literalNode.isBoolean())
      return this.owlDataFactory.getOWLLiteral(literalNode.getBooleanLiteralNode().getValue());
    else if (literalNode.isInteger())
      return this.owlDataFactory.getOWLLiteral(literalNode.getIntegerLiteralNode().getValue());
    else if (literalNode.isFloat())
      return this.owlDataFactory.getOWLLiteral(literalNode.getFloatLiteralNode().getValue());
    else if (literalNode.isString())
      return this.owlDataFactory.getOWLLiteral(literalNode.getStringLiteralNode().getValue());
    else
      throw new RendererException(
        "unknown OWL literal property value " + super.renderOWLPropertyValue(propertyValueNode));
  }

  private SpreadsheetLocation getLocation(SourceSpecificationNode sourceSpecificationNode) throws RendererException
  {
    if (sourceSpecificationNode.hasLiteral())
      return null;
    else
      return dataSource.resolveLocation(sourceSpecificationNode);
  }

  private String processLocationValue(SpreadsheetLocation location, ReferenceNode referenceNode, Rendering rendering)
    throws RendererException
  {
    String locationValue;
    SourceSpecificationNode sourceSpecification = referenceNode.getSourceSpecificationNode();

    if (sourceSpecification.hasLiteral()) {
      // Reference is a literal, e.g., @"Person", @"http://a.com#Person"
      locationValue = sourceSpecification.getLiteral();
      rendering.log("processReference: literal");
    } else { // Reference to data source location

      rendering.log("--processReference: specified location " + location);
      locationValue = dataSource.getLocationValue(location, referenceNode); // Deals with shifting

      if ((locationValue == null || locationValue.equals("")))
        locationValue = referenceNode.getActualDefaultLocationValue();

      if (locationValue.equals("") && referenceNode.getActualEmptyLocationDirective() == MM_ERROR_IF_EMPTY_LOCATION)
        throw new RendererException("empty location " + location + " in reference " + referenceNode);

      if (locationValue.equals("") && referenceNode.getActualEmptyLocationDirective() == MM_WARNING_IF_EMPTY_LOCATION)
        rendering.logLine("processReference: WARNING: empty location " + location + " in reference " + referenceNode);

      rendering.log(", location value [" + locationValue + "], entity type " + referenceNode.getEntityTypeNode());
    }

    if (!referenceNode.getEntityTypeNode().getEntityType().isOWLDataValue()) {
      rendering.log(", namespace " + getReferenceNamespace(referenceNode));
      String language = getReferenceLanguage(referenceNode);
      displayLanguage(language, rendering);
      rendering.log(", valueEncoding");
      for (ValueEncodingNode valueEncoding : referenceNode.getValueEncodingNodes())
        rendering.log(" " + valueEncoding);
    }

    if (!sourceSpecification.hasLiteral()) { // Determine if originally specified location has been shifted
      if (referenceNode.getActualShiftDirective() != MM_NO_SHIFT && referenceNode.hasShiftedLocation()) {
        location = referenceNode.getShiftedLocation();
        rendering.log(", shifted location " + referenceNode.getShiftedLocation());
      }
    }
    rendering.logLine("");

    return locationValue;
  }

  private String processRDFIDValue(String locationValue, ReferenceNode referenceNode, Rendering rendering)
    throws RendererException
  {
    String rdfIDValue;

    if (referenceNode.hasRDFIDValueEncoding()) {
      if (referenceNode.hasExplicitlySpecifiedRDFIDValueEncoding())
        rdfIDValue = processValueEncoding(locationValue, referenceNode.getRDFIDValueEncodingNode(), referenceNode);
      else if (referenceNode.hasValueExtractionFunction())
        rdfIDValue = processValueExtractionFunction(referenceNode.getValueExtractionFunctionNode(), locationValue);
      else
        rdfIDValue = locationValue;
    } else
      rdfIDValue = "";

    if (rdfIDValue.equals("") && !referenceNode.getActualDefaultRDFID().equals(""))
      rdfIDValue = referenceNode.getActualDefaultRDFID();

    if (rdfIDValue.equals("") && referenceNode.getActualEmptyRDFSLabelDirective() == MM_ERROR_IF_EMPTY_ID)
      throw new RendererException("empty RDF ID in reference " + referenceNode);

    if (rdfIDValue.equals("") && referenceNode.getActualEmptyRDFSLabelDirective() == MM_WARNING_IF_EMPTY_ID)
      rendering.logLine("processReference: WARNING: empty RDF ID in reference");

    return rdfIDValue;
  }

  private String processRDFSLabelText(String locationValue, ReferenceNode referenceNode, Rendering rendering)
    throws RendererException
  {
    String rdfsLabelText;

    if (referenceNode.hasRDFSLabelValueEncoding()) {
      if (referenceNode.hasExplicitlySpecifiedRDFSLabelValueEncoding())
        rdfsLabelText = processValueEncoding(locationValue, referenceNode.getRDFSLabelValueEncodingNode(),
          referenceNode);
      else if (referenceNode.hasValueExtractionFunction())
        rdfsLabelText = processValueExtractionFunction(referenceNode.getValueExtractionFunctionNode(), locationValue);
      else
        rdfsLabelText = locationValue;
    } else
      rdfsLabelText = "";

    if (rdfsLabelText.equals("") && !referenceNode.getActualDefaultRDFSLabel().equals(""))
      rdfsLabelText = referenceNode.getActualDefaultRDFSLabel();

    if (rdfsLabelText.equals("") && referenceNode.getActualEmptyRDFSLabelDirective() == MM_ERROR_IF_EMPTY_LABEL)
      throw new RendererException("empty RDFS label in reference " + referenceNode);

    if (rdfsLabelText.equals("") && referenceNode.getActualEmptyRDFSLabelDirective() == MM_WARNING_IF_EMPTY_LABEL)
      rendering.logLine("processReference: WARNING: empty RDFS label in reference");

    return rdfsLabelText;
  }

  private String processOWLDataValue(SpreadsheetLocation location, String locationValue, OWLEntityType entityType,
    ReferenceNode referenceNode, Rendering rendering) throws RendererException
  {
    String processedLocationValue = locationValue.replace("\"", "\\\"");
    String dataValue;

    if (referenceNode.hasLiteralValueEncoding()) {
      if (referenceNode.hasExplicitlySpecifiedLiteralValueEncoding())
        dataValue = processValueEncoding(processedLocationValue, referenceNode.getLiteralValueEncodingNode(),
          referenceNode);
      else if (referenceNode.hasValueExtractionFunction())
        dataValue = processValueExtractionFunction(referenceNode.getValueExtractionFunctionNode(),
          processedLocationValue);
      else
        dataValue = processedLocationValue;
    } else
      dataValue = "";

    if (dataValue.equals("") && !referenceNode.getActualDefaultDataValue().equals(""))
      dataValue = referenceNode.getActualDefaultDataValue();

    if (dataValue.equals("") && referenceNode.getActualEmptyDataValueDirective() == MM_ERROR_IF_EMPTY_DATA_VALUE)
      throw new RendererException("empty data value in reference " + referenceNode + " at location " + location);

    if (dataValue.equals("") && referenceNode.getActualEmptyDataValueDirective() == MM_WARNING_IF_EMPTY_DATA_VALUE)
      rendering.logLine(
        "processReference: WARNING: empty data value in reference " + referenceNode + " at location " + location);

    if (entityType.isQuotedOWLDataValue())
      dataValue = "\"" + dataValue + "\"";

    return dataValue;
  }

  private void displayLanguage(String language, Rendering rendering)
  {
    String display = "";

    if (language != null) {
      display += ", xml:lang";
      if (language.equals(""))
        display += "=mm:null";
      else if (!language.equals("*"))
        display += "=*";
      else if (language.equals("+"))
        display += "!=mm:null";
      else
        display += language;
    }
    rendering.log(display);
  }

  private String getReferenceNamespace(ReferenceNode referenceNode) throws RendererException
  {
    String namespace;

    // A reference will not have both a prefix and a namespace specified
    if (referenceNode.hasExplicitlySpecifiedPrefix()) {
      String prefix = referenceNode.getPrefixNode().getPrefix();
      namespace = getNamespaceForPrefix(prefix);
      if (namespace == null)
        throw new RendererException("unknown prefix " + prefix + " specified in reference " + referenceNode);
    } else if (referenceNode.hasExplicitlySpecifiedNamespace()) {
      namespace = referenceNode.getNamespaceNode().getNamespace();
    } else {
      if (!hasDefaultNamespace())
        throw new RendererException(
          "ontology has no default namespace and no namespace specified by reference " + referenceNode);

      namespace = getDefaultNamespace();
    }

    return namespace;
  }

  private String getReferenceLanguage(ReferenceNode referenceNode) throws RendererException
  {
    if (referenceNode.hasExplicitlySpecifiedLanguage())
      return referenceNode.getActualLanguage();
    else
      return getDefaultLanguage(); // Which might be null or empty
  }

  private String processValueEncoding(String value, ValueEncodingNode valueEncodingNode, ReferenceNode referenceNode)
    throws RendererException
  {
    String processedValue = "";

    if (valueEncodingNode != null) {
      if (valueEncodingNode.hasValueSpecification())
        processedValue = processValueSpecification(value, valueEncodingNode.getValueSpecification(), referenceNode);
      else
        processedValue = value;
    } else
      processedValue = value;

    return processedValue;
  }

  private String processValueSpecification(String value, ValueSpecificationNode valueSpecificationNode,
    ReferenceNode referenceNode) throws RendererException
  {
    String processedValue = "";

    for (ValueSpecificationItemNode valueSpecificationItemNode : valueSpecificationNode
      .getValueSpecificationItemNodes()) {
      if (valueSpecificationItemNode.hasStringLiteral())
        processedValue += valueSpecificationItemNode.getStringLiteral();
      else if (valueSpecificationItemNode.hasReference()) {
        ReferenceNode valueSpecificationItemReferenceNode = valueSpecificationItemNode.getReferenceNode();
        String referenceTextRendering;
        valueSpecificationItemReferenceNode.setDefaultShiftSetting(referenceNode.getActualShiftDirective());
        referenceTextRendering = renderReference(valueSpecificationItemReferenceNode).getRendering();
        if (valueSpecificationItemReferenceNode.getEntityTypeNode().getEntityType().isQuotedOWLDataValue()
          && !referenceTextRendering.equals("") && referenceTextRendering.startsWith("\""))
          processedValue += referenceTextRendering.substring(1, referenceTextRendering.length() - 1); // Strip quotes
          // from quoted
          // renderings
        else
          processedValue += referenceTextRendering;
      } else if (valueSpecificationItemNode.hasValueExtractionFunction()) {
        ValueExtractionFunctionNode valueExtractionFunction = valueSpecificationItemNode
          .getValueExtractionFunctionNode();
        processedValue += processValueExtractionFunction(valueExtractionFunction, value);
      } else if (valueSpecificationItemNode.hasCapturingExpression() && value != null) {
        String capturingExpression = valueSpecificationItemNode.getCapturingExpression();
        processedValue += processCapturingExpression(value, capturingExpression);
      }
    }

    return processedValue;
  }

  private String processCapturingExpression(String locationValue, String capturingExpression) throws RendererException
  {
    String result = "";

    try {
      Pattern p = Pattern.compile(capturingExpression); // Pull the value out of the location
      Matcher m = p.matcher(locationValue);
      boolean matchFound = m.find();
      if (matchFound) {
        for (int groupIndex = 1; groupIndex <= m.groupCount(); groupIndex++)
          result += (m.group(groupIndex));
      }
    } catch (PatternSyntaxException e) {
      throw new RendererException("invalid capturing expression: " + capturingExpression + ": " + e.getMessage());
    }
    return result;
  }

  private void addDefiningTypes(OWLEntityType entityType, OWLEntity entity, ReferenceNode referenceNode)
    throws RendererException
  {
    if (referenceNode.hasExplicitlySpecifiedTypes()) {
      for (TypeNode typeNode : referenceNode.getTypesNode().getTypeNodes()) {
        Rendering typeRendering = renderType(typeNode);
        if (!typeRendering.nothingRendered()) {
          String typeTextRendering = typeRendering.getRendering();
          if (entityType.isOWLClass()) {
            if (!entity.isOWLClass())
              throw new RendererException(
                "expecting class for type in reference " + referenceNode + " for " + entity + ", got " + entity
                  .getClass().getCanonicalName());
            String classExpressionID = typeTextRendering;
            OWLClassExpression classExpression = this.owlObjectHandler.getOWLClassExpression(classExpressionID);
            OWLSubClassOfAxiom axiom = this.owlDataFactory.getOWLSubClassOfAxiom(classExpression, entity.asOWLClass());
            addOWLAxiom(axiom);
          } else if (entityType.isOWLIndividual()) {
            if (!entity.isOWLNamedIndividual())
              throw new RendererException(
                "expecting individual for type in reference " + referenceNode + " for " + entity + ", got " + entity
                  .getClass().getCanonicalName());
            String classExpressionID = typeTextRendering;
            OWLClassExpression classExpression = this.owlObjectHandler.getOWLClass(classExpressionID);
            OWLClassAssertionAxiom axiom = this.owlDataFactory
              .getOWLClassAssertionAxiom(classExpression, entity.asOWLNamedIndividual());
            addOWLAxiom(axiom);
          } else if (entityType.isOWLObjectProperty()) {
            if (!entity.isOWLObjectProperty())
              throw new RendererException(
                "expecting object property for type in reference " + referenceNode + " for " + entity);
            String propertyShortName = typeTextRendering;
            OWLObjectProperty property = this.owlObjectHandler.getOWLObjectProperty(propertyShortName);
            OWLSubObjectPropertyOfAxiom axiom = owlDataFactory
              .getOWLSubObjectPropertyOfAxiom(property, entity.asOWLObjectProperty());
            addOWLAxiom(axiom);
          } else if (entityType.isOWLDataProperty()) {
            if (!entity.isOWLDataProperty())
              throw new RendererException(
                "expecting data property for type in reference " + referenceNode + " for " + entity);
            String propertyShortName = typeTextRendering;
            OWLDataProperty property = this.owlObjectHandler.getOWLDataProperty(propertyShortName);
            OWLSubDataPropertyOfAxiom axiom = owlDataFactory
              .getOWLSubDataPropertyOfAxiom(property, entity.asOWLDataProperty());
            addOWLAxiom(axiom);
          } else
            throw new RendererException("invalid entity type " + entityType);
        }
      }
    }
  }

  private OWLDataPropertyAssertionAxiom createOWLDataPropertyAssertionAxiom(OWLNamedIndividual subject,
    OWLDataProperty property, String propertyValue, OWLPropertyValueNode propertyValueNode) throws RendererException
  {
    if (propertyValueNode.isReference()) {
      ReferenceNode propertyValueReference = propertyValueNode.getReferenceNode();
      if (this.ontology.containsDataPropertyInSignature(property.getIRI())) {
        if (propertyValueReference.hasExplicitlySpecifiedEntityType()) {
          String typeShortName = propertyValueReference.getEntityTypeNode().getEntityType().getTypeName();
          OWLDatatype datatype = this.owlObjectHandler.getOWLDatatype(typeShortName);
          if (datatype.isString()) {
            String language = getReferenceLanguage(propertyValueReference);
            if (language != null) {
              OWLDataPropertyAssertionAxiom axiom = this.owlDataFactory
                .getOWLDataPropertyAssertionAxiom(property, subject, propertyValue);
              // TODO add language
              return axiom;
            } else {
              OWLLiteral literal = this.owlDataFactory.getOWLLiteral(propertyValue, datatype);
              return this.owlDataFactory.getOWLDataPropertyAssertionAxiom(property, subject, literal);
            }
          } else { // Data property but not a string
            OWLLiteral literal = this.owlDataFactory.getOWLLiteral(propertyValue, datatype);
            return this.owlDataFactory.getOWLDataPropertyAssertionAxiom(property, subject, literal);
          }
        } else { // No entity type explicitly specified
          return this.owlDataFactory.getOWLDataPropertyAssertionAxiom(property, subject, propertyValue);
        }
      } else
        throw new RendererException("property " + property + " is not an OWL data property");
    } else { // Not a reference - will be an entity name or a literal value
      if (ontology.containsDataPropertyInSignature(property.getIRI()))
        return this.owlDataFactory.getOWLDataPropertyAssertionAxiom(property, subject, propertyValue);
      else
        throw new RendererException("property " + property + " is not an OWL data property");
    }
  }

  private OWLObjectPropertyAssertionAxiom createOWLObjectPropertyAssertionAxiom(OWLNamedIndividual subject,
    OWLObjectProperty property, String propertyValue, OWLPropertyValueNode propertyValueNode) throws RendererException
  {
    if (propertyValueNode.isReference()) {
      if (ontology.containsObjectPropertyInSignature(property.getIRI())) {
        OWLNamedIndividual object = this.owlObjectHandler.getOWLNamedIndividual(propertyValue);
        return this.owlDataFactory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
      } else
        throw new RendererException("property " + property + " is not an OWL object or data property");
    } else { // Not a reference - will be an entity name
      if (ontology.containsObjectPropertyInSignature(property.getIRI())) {
        OWLNamedIndividual object = this.owlObjectHandler.getOWLNamedIndividual(propertyValue);
        return this.owlDataFactory.getOWLObjectPropertyAssertionAxiom(property, subject, object);
      } else
        throw new RendererException("property " + property + " is not an OWL object or data property");
    }
  }

  private OWLAnnotationAssertionAxiom createOWLAnnotationAssertionAxiom(OWLEntity entity,
    OWLAnnotationProperty property, String propertyValue, OWLPropertyValueNode propertyValueNode)
    throws RendererException
  {
    if (propertyValueNode.isReference()) {
      ReferenceNode propertyValueReference = propertyValueNode.getReferenceNode();
      if (propertyValueReference.hasExplicitlySpecifiedEntityType()) {
        String typeShortName = propertyValueReference.getEntityTypeNode().getEntityType().getTypeName();
        OWLDatatype datatype = this.owlObjectHandler.getOWLDatatype(typeShortName);
        if (datatype.isString()) {
          String language = getReferenceLanguage(propertyValueReference);
          if (language != null && !language.equals("")) {
            //TODO OWLAnnotationAssertionAxiom axiom = this.owlDataFactory.getOWLAnnotationAssertionAxiom(property, entity, propertyValue);
            // TODO Add language
            return null;
          } else {
            // TODO return this.owlDataFactory.getOWLAnnotationAssertionAxiom(property, entity, propertyValue, datatype);
            return null;
          }
        } else { // Not a string
          // TODO return this.owlDataFactory.getOWLAnnotationAssertionAxiom(property, entity, propertyValue, datatype);
          return null;
        }
      } else { // No entity type explicitly specified
        // TODO return this.owlDataFactory.getOWLAnnotationAssertionAxiom(property, entity, propertyValue);
        return null;
      }
    } else { // Not a reference - will be an entity name or a literal value
      // TODO return this.owlDataFactory.getOWLAnnotationAssertionAxiom(property, entity, propertyValue);
      return null;
    }
  }

  private String reverse(String source)
  {
    int i, len = source.length();
    StringBuffer dest = new StringBuffer(len);

    for (i = (len - 1); i >= 0; i--)
      dest.append(source.charAt(i));

    return dest.toString();
  }

  private void addOWLAxiom(OWLAxiom axiom)
  {
    // TODO Add axiom to ontology
  }

  private String getDefaultNamespace()
  {
    return ""; // TODO
  }

  private String getDefaultLanguage()
  {
    return ""; // TODO
  }

  private String getNamespaceForPrefix(String prefix)
  {
    return null; // TODO
  }

  private boolean hasDefaultNamespace()
  {
    return false; // TODO
  }
}
