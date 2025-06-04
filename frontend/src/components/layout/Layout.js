import SidePanel from '../SidePanel';
import Navbar from '../Navbar';
import Indicator from '../Indicator';

export default function Layout({ activePage, activeState, children }) {
    return (
        <>
            <div className="bg-eaeaea container-fluid position-fixed px-0" style={{ height: '100vh', width: '100vw' }}>
                <div className="d-flex" style={{ height: '100%' }}>
                    {/* Column-1 */}
                    <div style={{ width: '300px' }}>
                        <SidePanel activePage={activePage} activeState={activeState} />
                    </div>
                    {/* Column-2 */}
                    <div className='mx-3' style={{ width: 'calc(100% - 300px)', height: '100%', display: 'flex', flexDirection: 'column' }}>
                        <Navbar />
                        <Indicator />
                        <div className="bg-ffffff mb-3" style={{ flexGrow: 1, boxShadow: '0px 0px 24.7px 0px #00000026', borderRadius: '8px', overflow: 'hidden' }}>
                            <div className='d-flex' style={{ height: '100%' }}>
                                {children}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};