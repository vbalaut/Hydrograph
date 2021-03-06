/*******************************************************************************
 * Copyright 2017 Capital One Services, LLC and Bitwise, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

 
package hydrograph.ui.engine.converter.impl;

import hydrograph.engine.jaxb.commontypes.TypeBaseField;
import hydrograph.engine.jaxb.commontypes.TypeInputOutSocket;
import hydrograph.engine.jaxb.ihiveparquet.HivePartitionFieldsType;
import hydrograph.engine.jaxb.ihiveparquet.HivePartitionFilterType;
import hydrograph.engine.jaxb.ihiveparquet.HivePathType;
import hydrograph.engine.jaxb.ihiveparquet.HiveType;
import hydrograph.engine.jaxb.ihiveparquet.PartitionColumn;
import hydrograph.engine.jaxb.ihiveparquet.PartitionFieldBasicType;
import hydrograph.engine.jaxb.ihiveparquet.TypeInputDelimitedOutSocket;
import hydrograph.engine.jaxb.inputtypes.ParquetHiveFile;
import hydrograph.ui.common.util.Constants;
import hydrograph.ui.datastructure.property.GridRow;
import hydrograph.ui.datastructure.property.InputHivePartitionColumn;
import hydrograph.ui.datastructure.property.InputHivePartitionKeyValues;
import hydrograph.ui.engine.constants.PropertyNameConstants;
import hydrograph.ui.engine.converter.InputConverter;
import hydrograph.ui.graph.model.Component;
import hydrograph.ui.graph.model.Link;
import hydrograph.ui.logging.factory.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
/**
 * Converter implementation for Input Hive Parquet component
 * 
 * @author Bitwise
 */
public class InputHiveParquetConverter extends InputConverter {

	private static final Logger logger = LogFactory.INSTANCE.getLogger(InputHiveParquetConverter.class);
	Iterator<String> fieldIterator;
	ParquetHiveFile parquetHive;

	public InputHiveParquetConverter(Component component) {
		super(component);
		this.baseComponent = new ParquetHiveFile();
		this.component = component;
		this.properties = component.getProperties();
	}

	@Override
	public void prepareForXML(){
		logger.debug("Generating XML for {}", properties.get(Constants.PARAM_NAME));
		super.prepareForXML();
		parquetHive = (ParquetHiveFile) baseComponent;
		parquetHive.setRuntimeProperties(getRuntimeProperties());

		parquetHive.setDatabaseName(getHiveType(PropertyNameConstants.DATABASE_NAME.value()));
		parquetHive.setTableName(getHiveType(PropertyNameConstants.TABLE_NAME.value()));
		
		
		if(StringUtils.isNotBlank((String)properties.get(PropertyNameConstants.EXTERNAL_TABLE_PATH.value()))){
			parquetHive.setExternalTablePath(getHivePathType(PropertyNameConstants.EXTERNAL_TABLE_PATH.value()));
		}
		parquetHive.setPartitionKeys(getPartitionKeys());
		checkPartitionFilter();
	}
	
	private void checkPartitionFilter()
	{
		if(properties.get(PropertyNameConstants.PARTITION_KEYS.value())!=null){
			InputHivePartitionKeyValues property =(InputHivePartitionKeyValues) properties.get(PropertyNameConstants.PARTITION_KEYS.value());
			if(property.getKeyValues()!=null){
				if(!property.getKeyValues().isEmpty()){
					List<InputHivePartitionColumn> inputHivePartitionColumn=(List<InputHivePartitionColumn>)property.getKeyValues();
						if(!inputHivePartitionColumn.isEmpty()){
							parquetHive.setPartitionFilter(getPartitionFilter(inputHivePartitionColumn));
						}
				}
			}
		}
	}
	
	private HivePartitionFilterType getPartitionFilter(List<InputHivePartitionColumn> inputHivePartitionColumn){
			HivePartitionFilterType hivePartitionFilterType = new HivePartitionFilterType();
			List<PartitionColumn> partitionColumn = hivePartitionFilterType.getPartitionColumn();
			
			if(inputHivePartitionColumn!=null){
				for(InputHivePartitionColumn partcol :inputHivePartitionColumn){
					PartitionColumn pcol = new PartitionColumn();
					
					pcol.setName(partcol.getName());
					pcol.setValue(partcol.getValue());
					if(partcol.getInputHivePartitionColumn()!=null){
						addPartitionColumn(partcol,pcol);
					}
					partitionColumn.add(pcol);
				}
			}
			return hivePartitionFilterType;
	}
	
	private void  addPartitionColumn(InputHivePartitionColumn partcol,PartitionColumn pcol){
		InputHivePartitionColumn partitionColumn_rec=partcol.getInputHivePartitionColumn();
		PartitionColumn partc = new PartitionColumn();
		if(StringUtils.isNotBlank(partitionColumn_rec.getValue()))
		{
		partc.setName(partitionColumn_rec.getName());
		partc.setValue(partitionColumn_rec.getValue());
		pcol.setPartitionColumn(partc);
		}
		
		if(partitionColumn_rec.getInputHivePartitionColumn()!=null){
			if(StringUtils.isNotBlank(partitionColumn_rec.getValue()))
			{
				addPartitionColumn(partitionColumn_rec,partc);
			}else{
				addPartitionColumn(partitionColumn_rec,pcol);	
			}
		}
		
	}
	
	/*
	 * returns hiveType
	 */
	protected HiveType getHiveType(String propertyName) {
		logger.debug("Getting HypeType Value for {}={}", new Object[] {
				propertyName, properties.get(propertyName) });
		if (properties.get(propertyName) != null) {
			HiveType hiveType = new HiveType();
			hiveType.setValue(String.valueOf((String) properties
					.get(propertyName)));
			
				return hiveType;
		}
		return null;
	}
	
	/*
	 * returns hivePathType
	 */
	protected HivePathType getHivePathType(String propertyName) {
		logger.debug("Getting HypeType Value for {}={}", new Object[] {
				propertyName, properties.get(propertyName) });
		if (properties.get(propertyName) != null) {
			HivePathType hivePathType = new HivePathType();
			hivePathType.setUri(String.valueOf((String) properties
					.get(propertyName)));
			
				return hivePathType;
		}
		return null;
	}
	
	/*
	 * returns HivePartitionFieldsType
	 */
	private HivePartitionFieldsType getPartitionKeys() {
		if (properties.get(PropertyNameConstants.PARTITION_KEYS.value()) != null) {
			InputHivePartitionKeyValues property = (InputHivePartitionKeyValues) properties
					.get(PropertyNameConstants.PARTITION_KEYS.value());
			if (property.getKey() != null) {
				if (!property.getKey().isEmpty()) {
					List<String> fieldValueSet = new ArrayList<String>();
					fieldValueSet = (List<String>) property.getKey();

					HivePartitionFieldsType hivePartitionFieldsType = new HivePartitionFieldsType();
					PartitionFieldBasicType partitionFieldBasicType = new PartitionFieldBasicType();
					hivePartitionFieldsType.setField(partitionFieldBasicType);

					if (fieldValueSet != null) {
						fieldIterator = fieldValueSet.iterator();
						if (fieldIterator.hasNext()) {
							partitionFieldBasicType.setName((String) fieldIterator.next());
						}
						if (fieldIterator.hasNext()) {
							addPartitionKey(partitionFieldBasicType);
						}
					}
					return hivePartitionFieldsType;
				}
			}
		}
		return null;
	}
	
	private void addPartitionKey(PartitionFieldBasicType partfbasic){
		PartitionFieldBasicType field = new PartitionFieldBasicType();
		field.setName((String)fieldIterator.next());
		partfbasic.setField(field);
		if(fieldIterator.hasNext()){
			addPartitionKey(field);
		}
	}
	
	@Override
	protected List<TypeInputOutSocket> getInOutSocket() {
		logger.debug("Generating TypeInputOutSocket data for {}", properties.get(Constants.PARAM_NAME));
		List<TypeInputOutSocket> outSockets = new ArrayList<>();
		for (Link link : component.getSourceConnections()) {
			TypeInputDelimitedOutSocket outSocket = new TypeInputDelimitedOutSocket();
			outSocket.setId(link.getSourceTerminal());
			outSocket.setType(link.getSource().getPort(link.getSourceTerminal()).getPortType());
			outSocket.setSchema(getSchema());
			outSocket.getOtherAttributes();
			outSockets.add(outSocket);
		}
		return outSockets;
	}

	@Override
	protected List<TypeBaseField> getFieldOrRecord(List<GridRow> gridList) {
		logger.debug("Generating data for {} for property {}", new Object[] { properties.get(Constants.PARAM_NAME),
				PropertyNameConstants.SCHEMA.value() });

		List<TypeBaseField> typeBaseFields = new ArrayList<>();
		if (gridList != null && gridList.size() != 0) {
			for (GridRow object : gridList) {
				typeBaseFields.add(converterHelper.getSchemaGridTargetData(object));
			}
		}
		return typeBaseFields;
	}
}

