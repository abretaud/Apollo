package org.bbop.apollo

import grails.converters.JSON
import grails.transaction.Transactional
import org.bbop.apollo.gwt.shared.PermissionEnum
import org.bbop.apollo.report.AnnotatorSummary
import org.bbop.apollo.report.OrganismPermissionSummary
import org.bbop.apollo.report.OrganismSummary
import org.bbop.apollo.report.SequenceSummary
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.parser.JSONParser
import org.json.JSONObject

@Transactional
class ReportService {

    def permissionService

    def generateAllFeatureSummary() {
        OrganismSummary thisFeatureSummaryInstance = new OrganismSummary()
        thisFeatureSummaryInstance.geneCount = Gene.count


        Map<String, Integer> transcriptMap = new TreeMap<>()
        Transcript.executeQuery("select distinct g from Transcript g ").each {
            String className = it.class.canonicalName.substring("org.bbop.apollo.".size())
            Integer count = transcriptMap.get(className) ?: 0
            transcriptMap.put(className, ++count)
        }
        thisFeatureSummaryInstance.transcriptTypeCount = transcriptMap
        if (transcriptMap) {
            thisFeatureSummaryInstance.transcriptCount = transcriptMap.values()?.sum()
        } else {
            thisFeatureSummaryInstance.transcriptCount = 0
        }

        thisFeatureSummaryInstance.transcriptCount = Transcript.count
        thisFeatureSummaryInstance.transposableElementCount = TransposableElement.count
        thisFeatureSummaryInstance.repeatRegionCount = RepeatRegion.count
        thisFeatureSummaryInstance.exonCount = Exon.count
        thisFeatureSummaryInstance.sequenceCount = Sequence.count



        return thisFeatureSummaryInstance
    }

    def generateOrganismSummary(Organism organism) {
        OrganismSummary thisFeatureSummaryInstance = new OrganismSummary()
        thisFeatureSummaryInstance.geneCount = (int) Gene.executeQuery("select count(distinct g) from Gene g join g.featureLocations fl join fl.sequence s join s.organism o where o = :organism", [organism: organism]).iterator().next()

        thisFeatureSummaryInstance.annotators = User.executeQuery("select distinct own from Feature g join g.featureLocations fl join fl.sequence s join s.organism o join g.owners own where o = :organism", [organism: organism])


        Map<String, Integer> transcriptMap = new TreeMap<>()
        Transcript.executeQuery("select distinct g from Transcript g join g.featureLocations fl join fl.sequence s join s.organism o where o = :organism", [organism: organism]).each {
            String className = it.class.canonicalName.substring("org.bbop.apollo.".size())
            Integer count = transcriptMap.get(className) ?: 0
            transcriptMap.put(className, ++count)
        }
        thisFeatureSummaryInstance.transcriptTypeCount = transcriptMap
        if (transcriptMap) {
            thisFeatureSummaryInstance.transcriptCount = transcriptMap.values()?.sum()
        } else {
            thisFeatureSummaryInstance.transcriptCount = 0
        }
        thisFeatureSummaryInstance.sequenceCount = Sequence.countByOrganism(organism)
        thisFeatureSummaryInstance.organismId = organism.id

        thisFeatureSummaryInstance.transposableElementCount = (int) TransposableElement.executeQuery("select count(distinct g) from TransposableElement g join g.featureLocations fl join fl.sequence s join s.organism o where o = :organism", [organism: organism]).iterator().next()
        thisFeatureSummaryInstance.repeatRegionCount = (int) RepeatRegion.executeQuery("select count(distinct g) from RepeatRegion g join g.featureLocations fl join fl.sequence s join s.organism o where o = :organism", [organism: organism]).iterator().next()
        thisFeatureSummaryInstance.exonCount = (int) Exon.executeQuery("select count(distinct g) from Exon g join g.featureLocations fl join fl.sequence s join s.organism o where o = :organism", [organism: organism]).iterator().next()
        return thisFeatureSummaryInstance

    }


    def generateSequenceSummary(Sequence sequence) {
        SequenceSummary sequenceSummary = new SequenceSummary()
        sequenceSummary.sequence = sequence
        sequenceSummary.geneCount = (int) Gene.executeQuery("select count(g) from Gene g join g.featureLocations fl join fl.sequence s where s = :sequence ", [sequence: sequence]).iterator().next()
        sequenceSummary.transposableElementCount = (int) TransposableElement.executeQuery("select count(g) from TransposableElement  g join g.featureLocations fl join fl.sequence s where s = :sequence ", [sequence: sequence]).iterator().next()
        sequenceSummary.repeatRegionCount = (int) RepeatRegion.executeQuery("select count(g) from RepeatRegion  g join g.featureLocations fl join fl.sequence s where s = :sequence ", [sequence: sequence]).iterator().next()
        sequenceSummary.exonCount = (int) Exon.executeQuery("select count(g) from Exon g join g.featureLocations fl join fl.sequence s where s = :sequence ", [sequence: sequence]).iterator().next()
        sequenceSummary.annotators = User.executeQuery("select distinct annotator from Feature g join g.featureLocations fl join fl.sequence s join g.owners annotator where s = :sequence ", [sequence: sequence])


        Map<String, Integer> transcriptMap = new TreeMap<>()
        Transcript.executeQuery("select distinct g from Transcript g join g.featureLocations fl join fl.sequence s  where s = :sequence", [sequence: sequence]).each {
            String className = it.class.canonicalName.substring("org.bbop.apollo.".size())
            Integer count = transcriptMap.get(className) ?: 0
            transcriptMap.put(className, ++count)
        }
        sequenceSummary.transcriptTypeCount = transcriptMap
        if (transcriptMap) {
            sequenceSummary.transcriptCount = transcriptMap.values()?.sum()
        } else {
            sequenceSummary.transcriptCount = 0
        }


        return sequenceSummary
    }

    def copyProperties(source, target) {
        source.properties.each { key, value ->
            if (target.hasProperty(key) && !(key in ['class', 'metaClass'])) {
                try {
                    target[key] = value
                }
                catch (ReadOnlyPropertyException rpo) {
                }
            }
        }
    }

    AnnotatorSummary generateAnnotatorSummary(User owner, boolean includePermissions = false) {
        AnnotatorSummary annotatorSummary = new AnnotatorSummary()
        annotatorSummary.annotator = owner
        annotatorSummary.geneCount = (int) Gene.executeQuery("select count(distinct g) from Gene g join g.owners owner where owner = :owner ", [owner: owner]).iterator().next()
        annotatorSummary.transposableElementCount = (int) TransposableElement.executeQuery("select count(distinct g) from TransposableElement g join g.owners owner where owner = :owner", [owner: owner]).iterator().next()
        annotatorSummary.repeatRegionCount = (int) TransposableElement.executeQuery("select count(distinct g) from RepeatRegion g join g.owners owner where owner = :owner", [owner: owner]).iterator().next()
        annotatorSummary.exonCount = (int) TransposableElement.executeQuery("select count(distinct g) from Exon g join g.childFeatureRelationships child join child.parentFeature.owners owner where owner = :owner", [owner: owner]).iterator().next()


        Map<String, Integer> transcriptMap = new TreeMap<>()
        Transcript.executeQuery("select distinct g from Transcript g join g.owners owner where owner = :owner ", [owner: owner]).each {
            String className = it.class.canonicalName.substring("org.bbop.apollo.".size())
            Integer count = transcriptMap.get(className) ?: 0
            transcriptMap.put(className, ++count)
        }
        annotatorSummary.transcriptTypeCount = transcriptMap
        if (transcriptMap) {
            annotatorSummary.transcriptCount = transcriptMap.values()?.sum()
        } else {
            annotatorSummary.transcriptCount = 0
        }

        // TODO: add groups as well
        if (includePermissions && !permissionService.isUserAdmin(owner)) {

            List<OrganismPermissionSummary> userOrganismPermissionList = new ArrayList<>()
            if (permissionService.isUserAdmin(owner)) {
                Organism.listOrderByCommonName().each {
                    OrganismPermissionSummary organismPermissionSummary = new OrganismPermissionSummary()
                    UserOrganismPermission userOrganismPermission = new UserOrganismPermission()
                    userOrganismPermission.permissions = [PermissionEnum.ADMINISTRATE, PermissionEnum.EXPORT, PermissionEnum.READ, PermissionEnum.WRITE]
                    userOrganismPermission.organism = it
                    organismPermissionSummary.userOrganismPermission = userOrganismPermission
                    copyProperties(generateOrganismSummary(it), organismPermissionSummary)
                    userOrganismPermissionList.add(organismPermissionSummary)
                }
            } else {
                UserOrganismPermission.findAllByUser(owner).each {
                    OrganismPermissionSummary organismPermissionSummary = new OrganismPermissionSummary()
                    organismPermissionSummary.userOrganismPermission = it
                    copyProperties(generateOrganismSummary(it.organism), organismPermissionSummary)

                    userOrganismPermissionList.add(organismPermissionSummary)
                }
            }

            owner.userGroups.each { group ->
                for (GroupOrganismPermission groupPermission in GroupOrganismPermission.findAllByGroup(group)) {
                    // minimally, you should have at least one permission
                    if (groupPermission.permissions) {
                        OrganismPermissionSummary organismPermissionSummary = new OrganismPermissionSummary()
                        UserOrganismPermission userOrganismPermission = new UserOrganismPermission()
                        userOrganismPermission.permissions = groupPermission.permissions
                        userOrganismPermission.organism = groupPermission.organism
                        organismPermissionSummary.userOrganismPermission = userOrganismPermission
                        copyProperties(generateOrganismSummary(groupPermission.organism), organismPermissionSummary)
                        userOrganismPermissionList.add(organismPermissionSummary)
                    }
                }

            }

            annotatorSummary.userOrganismPermissionList = mergePermissions(userOrganismPermissionList)
        }



        return annotatorSummary
    }

    Map<Organism, AnnotatorSummary> generateAnnotatorSummary2(User owner, List<Organism> organisms, boolean includePermissions = false) {
        Map<Organism, AnnotatorSummary> summaryMap = new HashMap<>()
        //List<AnnotatorSummary> summaries = new ArrayList<>()
        // get features created by the annotator
        def genes = Gene.executeQuery("select distinct g from Gene g join g.owners owner where owner = :owner", [owner: owner])
        def transposableElement = TransposableElement.executeQuery("select distinct g from TransposableElement g join g.owners owner where owner = :owner", [owner: owner])
        def repeatRegion = TransposableElement.executeQuery("select distinct g from RepeatRegion g join g.owners owner where owner = :owner", [owner: owner])
        def exons = TransposableElement.executeQuery("select distinct g from Exon g join g.childFeatureRelationships child join child.parentFeature.owners owner where owner = :owner", [owner: owner])
        def transcripts = Transcript.executeQuery("select distinct g from Transcript g join g.owners owner where owner = :owner ", [owner: owner])
        organisms.each { organism ->
            AnnotatorSummary annotatorSummary = new AnnotatorSummary()
            annotatorSummary.annotator = owner
            //get the gene on the current organism
            def currentGenes = genes.findAll() {
                it.featureLocations.sequence.organism.id.iterator().next() == organism.id
            }
            def currentTE = transposableElement.findAll() {
                it.featureLocations.sequence.organism.id.iterator().next() == organism.id
            }
            def currentRR = repeatRegion.findAll() {
                it.featureLocations.sequence.organism.id.iterator().next() == organism.id
            }
            def currentExons = exons.findAll() {
                it.featureLocations.sequence.organism.id.iterator().next() == organism.id
            }
            def currentTranscripts = transcripts.findAll() {
                it.featureLocations.sequence.organism.id.iterator().next() == organism.id
            }
            annotatorSummary.geneCount = (int) currentGenes.size()
            annotatorSummary.transposableElementCount = (int) currentTE.size()
            annotatorSummary.repeatRegionCount = (int) currentRR.size()
            annotatorSummary.exonCount = (int) currentExons.size()

            Map<String, Integer> transcriptMap = new TreeMap<>()
            currentTranscripts.each {
                String className = it.class.canonicalName.substring("org.bbop.apollo.".size())
                Integer count = transcriptMap.get(className) ?: 0
                transcriptMap.put(className, ++count)
            }
            annotatorSummary.transcriptTypeCount = transcriptMap
            if (transcriptMap) {
                annotatorSummary.transcriptCount = transcriptMap.values()?.sum()
            } else {
                annotatorSummary.transcriptCount = 0
            }

            if (includePermissions && !permissionService.isUserAdmin(owner)) {

                List<OrganismPermissionSummary> userOrganismPermissionList = new ArrayList<>()
                if (permissionService.isUserAdmin(owner)) {
                    Organism.listOrderByCommonName().each {
                        OrganismPermissionSummary organismPermissionSummary = new OrganismPermissionSummary()
                        UserOrganismPermission userOrganismPermission = new UserOrganismPermission()
                        userOrganismPermission.permissions = [PermissionEnum.ADMINISTRATE, PermissionEnum.EXPORT, PermissionEnum.READ, PermissionEnum.WRITE]
                        userOrganismPermission.organism = it
                        organismPermissionSummary.userOrganismPermission = userOrganismPermission
                        copyProperties(generateOrganismSummary(it), organismPermissionSummary)
                        userOrganismPermissionList.add(organismPermissionSummary)
                    }
                } else {
                    UserOrganismPermission.findAllByUser(owner).each {
                        OrganismPermissionSummary organismPermissionSummary = new OrganismPermissionSummary()
                        organismPermissionSummary.userOrganismPermission = it
                        copyProperties(generateOrganismSummary(it.organism), organismPermissionSummary)

                        userOrganismPermissionList.add(organismPermissionSummary)
                    }
                }

                owner.userGroups.each { group ->
                    for (GroupOrganismPermission groupPermission in GroupOrganismPermission.findAllByGroup(group)) {
                        // minimally, you should have at least one permission
                        if (groupPermission.permissions) {
                            OrganismPermissionSummary organismPermissionSummary = new OrganismPermissionSummary()
                            UserOrganismPermission userOrganismPermission = new UserOrganismPermission()
                            userOrganismPermission.permissions = groupPermission.permissions
                            userOrganismPermission.organism = groupPermission.organism
                            organismPermissionSummary.userOrganismPermission = userOrganismPermission
                            copyProperties(generateOrganismSummary(groupPermission.organism), organismPermissionSummary)
                            userOrganismPermissionList.add(organismPermissionSummary)
                        }
                    }

                }

                annotatorSummary.userOrganismPermissionList = mergePermissions(userOrganismPermissionList)
            }
            summaryMap.put(organism, annotatorSummary)
        }
        return summaryMap
    }


    List<OrganismPermissionSummary> mergePermissions(ArrayList<OrganismPermissionSummary> organismPermissionSummaries) {
        Map<String,OrganismPermissionSummary> map = new TreeMap<>()

        organismPermissionSummaries.each {
            String organismName = it.userOrganismPermission.organism.commonName
            OrganismPermissionSummary organismPermissionSummary = null
            if(map.containsKey(organismName)){
                organismPermissionSummary = map.get(organismName)
                String finalPermissions = mergePermissionStrings(organismPermissionSummary.userOrganismPermission.permissions,it.userOrganismPermission.permissions)
                organismPermissionSummary.userOrganismPermission.permissions = finalPermissions
            }
            else{
                organismPermissionSummary = it
            }
            map.put(organismName,organismPermissionSummary)
        }

        return map.values() as List
    }

    String mergePermissionStrings(String s1, String s2) {
        Set<String> permissions = new TreeSet<>()
        def array1 = JSON.parse(s1) as JSONArray
        def array2 = JSON.parse(s2) as JSONArray
        for(int i = 0 ; i < array1.size() ; i++){
            permissions.add(array1.getString(i))
        }
        for(int i = 0 ; i < array2.size() ; i++){
            permissions.add(array2.getString(i))
        }
        JSONArray returnArray = new JSONArray()
        permissions.each {
            returnArray.add(it)
        }
        return returnArray.toString()
    }
}
