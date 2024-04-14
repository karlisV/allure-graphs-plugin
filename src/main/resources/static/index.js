'use strict';
const CoverageModel = Backbone.Collection.extend({
    url: 'data/coverage.json',
})

class CoverageHelpers {
    groupEndpointsByRootPath(endpointList){
       const endpointsByRootPath = endpointList.reduce((acc, endpoint) => {
            const parts = endpoint.url.split(' ');
            const method = parts[0];
            const fullPath = parts[1];
            const rootPathSegment = fullPath.split('/')[1];
            const rootPath = rootPathSegment ? rootPathSegment.toUpperCase() : 'ROOT';
    
            if (!acc[rootPath]) {
                acc[rootPath] = { endpoints: [], endpointCoverageCount: 0, totalEndpoints: 0 };
            }
            acc[rootPath].endpoints.push({
                method: method,
                path: fullPath,
                hitCount: endpoint.hit_count
            });
            
            if (endpoint.hit_count > 0) {
                acc[rootPath].endpointCoverageCount += 1;
            }
            acc[rootPath].totalEndpoints += 1;
    
            return acc;
        }, {});

        for (let rootPath in endpointsByRootPath) {
            endpointsByRootPath[rootPath].endpoints.sort((a, b) => a.path.localeCompare(b.path));
        }
        return endpointsByRootPath;
    }
}

const template = function (data) {
    const totalCoverage = data.items.coverage;
    const documentedEndpointsBySection = data.helpers.groupEndpointsByRootPath(data.items.documented_endpoints);
    const notDocumentedEndpointsBySection = data.helpers.groupEndpointsByRootPath(data.items.not_documented_endpoints);
    console.log(notDocumentedEndpointsBySection);



    let html = '<h3 class="pane__title">Coverage</h3>';
    html += `<div class="total-coverage">Total Coverage: <strong>${totalCoverage}%</strong></div>`;
    html += '<div class="coverage-container">';

    Object.keys(documentedEndpointsBySection).sort().forEach(section => {
        const { endpoints, endpointCoverageCount, totalEndpoints } = documentedEndpointsBySection[section];
        let sectionClass = 'red'; 
        if (endpointCoverageCount === totalEndpoints) {
            sectionClass = 'green';
        } else if (endpointCoverageCount > 0) {
            sectionClass = 'yellow';
        }

        html += `<h4 class="root-path-header ${sectionClass}">${section}</h4>`;
        html += '<ul class="url-list-container">';
        endpoints.forEach(({ method, path, hitCount }) => {
            let liClass = hitCount > 0 ? 'hit' : 'miss';
            html += `<li class="${liClass}"><span class="method">${method}</span> <span class="path">${path}</span><span class="count"> called <strong>${hitCount}</strong> times</span></li>`;
        });
        html += '</ul>';
    });

    if(notDocumentedEndpointsBySection && notDocumentedEndpointsBySection.length === 0) return;

    html += `<div class="not-documented-endpoints"><h4 class="root-path-header">Not documented in Swagger</h4><ul class="url-list-container">`;
    Object.keys(notDocumentedEndpointsBySection).sort().forEach(section => {
        const { endpoints } = notDocumentedEndpointsBySection[section]
        endpoints.forEach(({method, path}) => {
            html += `<li><span class="method">${method}</span> <span class="path">${path}</span></li>`;
        })
    })
    html += `</ul></div>`;

    html += "</div>"; 
    return html;
};

const CoverageView = Backbone.Marionette.View.extend({
    template: template,


    render: function () {
        this.$el.html(this.template(this.options));
        return this;
    },
});

class CoverageLayout extends allure.components.AppLayout {

    initialize() {
        this.model = new CoverageModel();
        this.helpers = new CoverageHelpers();
    }

    loadData() {
        return this.model.fetch();
    }

    getContentView() {
        return new CoverageView({items: this.model.models[0].attributes, helpers: this.helpers});
    }
}

allure.api.addTranslation('en', {
    tab: {
        coverage: {
            name: 'Coverage'
        }
    }
});

allure.api.addTab('coverage', {
    title: 'tab.coverage.name', icon: 'fa fa-bar-chart',
    route: 'coverage',
    onEnter: (function () {
        return new CoverageLayout()
    })
});