/*
 *                   GridGain Community Edition Licensing
 *                   Copyright 2019 GridGain Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License") modified with Commons Clause
 * Restriction; you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * 
 * Commons Clause Restriction
 * 
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 * 
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including without
 * limitation fees for hosting or consulting/ support services related to the Software), a product or
 * service whose value derives, entirely or substantially, from the functionality of the Software.
 * Any license notice or attribution required by the License must also include this Commons Clause
 * License Condition notice.
 * 
 * For purposes of the clause above, the “Licensor” is Copyright 2019 GridGain Systems, Inc.,
 * the “License” is the Apache License, Version 2.0, and the Software is the GridGain Community
 * Edition software provided with this notice.
 */

import _ from 'lodash';

import { BehaviorSubject } from 'rxjs';
import {tap, filter, combineLatest} from 'rxjs/operators';
import {CancellationError} from 'app/errors/CancellationError';

export default class {
    static $inject = ['AgentManager', 'IgniteConfirm', 'IgniteVersion', 'IgniteMessages'];

    /**
     * @param agentMgr Agent manager.
     * @param Confirm Confirmation service.
     * @param Version Version check service.
     * @param Messages Messages service.
     */
    constructor(agentMgr, Confirm, Version, Messages) {
        this.agentMgr = agentMgr;
        this.Confirm = Confirm;
        this.Version = Version;
        this.Messages = Messages;

        this.clusters = [];
        this.isDemo = agentMgr.isDemoMode();
        this._inProgressSubject = new BehaviorSubject(false);
    }

    $onInit() {
        if (this.isDemo)
            return;

        this.inProgress$ = this._inProgressSubject.asObservable();

        this.clusters$ = this.agentMgr.connectionSbj.pipe(
            combineLatest(this.inProgress$),
            tap(([sbj, inProgress]) => this.inProgress = inProgress),
            filter(([sbj, inProgress]) => !inProgress),
            tap(([{cluster, clusters}]) => {
                this.cluster = cluster ? {...cluster} : null;
                this.clusters = _.orderBy(clusters, ['name'], ['asc']);
            })
        )
        .subscribe(() => {});
    }

    $onDestroy() {
        if (!this.isDemo)
            this.clusters$.unsubscribe();
    }

    change(item) {
        this.agentMgr.switchCluster(item)
            .then(() => this.cluster = item)
            .catch((err) => {
                if (!(err instanceof CancellationError))
                    this.Messages.showError('Failed to switch cluster: ', err);
            });
    }

    isChangeStateAvailable() {
        return !this.isDemo && this.cluster && this.Version.since(this.cluster.clusterVersion, '2.0.0');
    }

    toggle($event) {
        $event.preventDefault();

        const toggleClusterState = () => {
            this._inProgressSubject.next(true);

            return this.agentMgr.toggleClusterState()
                .then(() => this._inProgressSubject.next(false))
                .catch((err) => {
                    this._inProgressSubject.next(false);

                    this.Messages.showError('Failed to toggle cluster state: ', err);
                });
        };

        if (this.cluster.active) {
            return this.Confirm.confirm('Are you sure you want to deactivate cluster?')
                .then(() => toggleClusterState());
        }

        return toggleClusterState();
    }
}
